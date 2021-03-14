package io.github.hoeggi.openshiftdb.server.handler.postgres

import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.postgres.DatabaseDownloaderCustom
import io.github.hoeggi.openshiftdb.postgres.DatabaseDownloaderPlain
import io.github.hoeggi.openshiftdb.postgres.Postgres
import io.github.hoeggi.openshiftdb.postgres.PostgresPrincibal
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private suspend fun SendChannel<Frame>.sendAsText(message: DatabaseDownloadMessage) {
    send(Frame.Text(Json.encodeToString(message)))
}

private suspend fun WebSocketSession.close(code: CloseReason.Codes, message: DatabaseDownloadMessage) {
    close(CloseReason(code, Json.encodeToString(message)))
}

private val logger = LoggerFactory.getLogger("io.github.hoeggi.openshiftdb.server.handler.postgres.PostgresDump")

internal fun PostgresDump(): suspend DefaultWebSocketServerSession.() -> Unit = {

    try {
        val principal = call.principal<PostgresPrincibal>()
        val username = principal?.username
        val password = principal?.password

        val parameters = call.parameters
        val database = parameters["database"]
        val path = parameters["path"]
        val format = parameters["format"]

        if (principal == null || username == null || password == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else if (database == null || path == null) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            when (format) {
                "p", "plain" -> downloadPlain(database, path, username, password, this)
                else -> downloadCustom(database, path, username, password, this)
            }
        }
    } catch (e: ClosedReceiveChannelException) {
        logger.debug("ClosedReceiveChannelException ${closeReason.await()}", e)
    } catch (e: Throwable) {
        logger.error("unexpected error", e)
    }
}

private suspend fun downloadPlain(
    database: String,
    path: String,
    username: String,
    password: String,
    session: DefaultWebSocketSession,
) {
    val outgoing = session.outgoing
    val incoming = session.incoming
    val dump = Postgres.dumpDatabasePlain(database, path, username, password)
    val downloader = DatabaseDownloaderPlain(dump,
        onNewLine = {
            logger.debug("onNewLine: $it")
            if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseDownloadMessage.inprogress(it))
            else dump.close()
        },
        onSuccess = {
            logger.debug("onSuccessLine: $it")
            if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseDownloadMessage.finish(it))
            else dump.close()
        },
        onError = { code, ex ->
            logger.error("onError: $code", ex)
            session.close(
                CloseReason.Codes.INTERNAL_ERROR,
                DatabaseDownloadMessage.error("code: $code - message: ${ex?.message}")
            )
        })
    val async = session.async {
        if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseDownloadMessage.start())
        downloader.download()
    }
    val incomingMessages = session.async(Dispatchers.IO) {
        logger.debug("start receiver")
        while (true) {
            val receive = incoming.receiveOrNull() ?: run {
                logger.debug("received null, shutting down")
                dump.close()
                return@async
            }
            if (receive.frameType == FrameType.TEXT) {
                val text = (receive as Frame.Text).readText()
                logger.debug("received unexpected text frame: $text")
            }
        }
    }
    async.await()
    incomingMessages.await()
    logger.debug("channel closed ${session.closeReason.await()}")
}

private suspend fun downloadCustom(
    database: String,
    path: String,
    username: String,
    password: String,
    session: DefaultWebSocketSession,
) {
    val outgoing = session.outgoing
    val incoming = session.incoming
    val dump = Postgres.dumpDatabaseCustom(database, path, username, password)
    val downloader = DatabaseDownloaderCustom(dump,
        onSuccess = {
            logger.debug("onSuccess")
            if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseDownloadMessage.finish(dump.output))
        },
        onError = { code, message, ex ->
            logger.error("onError: $code", ex)
            session.close(
                CloseReason.Codes.INTERNAL_ERROR,
                DatabaseDownloadMessage.error("code: $code - message: ${message ?: ex?.message}")
            )
        })
    val async = session.async {
        if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseDownloadMessage.start())
        downloader.download()
    }
    val incomingMessages = session.async(Dispatchers.IO) {
        logger.debug("start receiver")
        while (true) {
            val receive = incoming.receiveOrNull() ?: run {
                logger.debug("received null, shutting down")
                return@async
            }
            if (receive.frameType == FrameType.TEXT) {
                val text = (receive as Frame.Text).readText()
                logger.debug("received unexpected text frame: $text")
            }
        }
    }
    async.await()
    incomingMessages.await()
    logger.debug("channel closed ${session.closeReason.await()}")
}