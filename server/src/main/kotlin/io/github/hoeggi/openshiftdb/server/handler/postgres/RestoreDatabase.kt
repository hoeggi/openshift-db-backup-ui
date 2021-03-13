package io.github.hoeggi.openshiftdb.server.handler.postgres

import io.github.hoeggi.openshiftdb.api.response.DatabaseRestoreMessage
import io.github.hoeggi.openshiftdb.api.response.ProjectApi
import io.github.hoeggi.openshiftdb.api.response.RestoreRequestAPI
import io.github.hoeggi.openshiftdb.postgres.DatabaseRestore
import io.github.hoeggi.openshiftdb.postgres.Postgres
import io.github.hoeggi.openshiftdb.postgres.PostgresPrincibal
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.websocket.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private suspend fun SendChannel<Frame>.sendAsText(message: DatabaseRestoreMessage) {
    send(Frame.Text(Json.encodeToString(message)))
}

private fun Frame.Text.readTextOrNull(): DatabaseRestoreMessage? = try {
    Json.decodeFromString(readText())
} catch (ex: SerializationException) {
    null
}

private suspend fun WebSocketSession.close(code: CloseReason.Codes, message: DatabaseRestoreMessage) {
    close(CloseReason(code, Json.encodeToString(message)))
}

private val logger = LoggerFactory.getLogger("io.github.hoeggi.openshiftdb.server.handler.postgres.RestoreDatabase")

internal fun RestoreDatabase(): suspend DefaultWebSocketServerSession.() -> Unit = {

    try {
        val principal = call.principal<PostgresPrincibal>()
        val username = principal?.username
        val password = principal?.password

        val database = call.request.queryParameters["database"]
        val exists = call.request.queryParameters["exists"]
        val path = call.request.queryParameters["path"]

        if (principal == null || username == null || password == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else if (database == null || exists == null || path == null) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            val restoreDatabase =
                Postgres.restoreDatabase(username, password, path, database, exists.toBoolean())
            restore(restoreDatabase, this)
        }
    } catch (e: ClosedReceiveChannelException) {
        logger.debug("ClosedReceiveChannelException ${closeReason.await()}", e)
    } catch (e: Throwable) {
        logger.error("unexpected error", e)
    }
}

private suspend fun restore(
    restore: Postgres.PostgresResult.Restore,
    session: DefaultWebSocketSession,
) {
    val outgoing = session.outgoing
    val incoming = session.incoming
    val downloader = DatabaseRestore(restore,
        onNewLine = {
            logger.debug("onNewLine: $it")
            if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseRestoreMessage.inprogress(it))
            else restore.close()
        },
        onSuccess = { backup, database ->
            logger.debug("restored database: $database from $backup")
            if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseRestoreMessage.finish())
            else restore.close()
        },
        onError = { code, ex ->
            logger.error("onError: $code", ex)
            session.close(
                CloseReason.Codes.INTERNAL_ERROR,
                DatabaseRestoreMessage.error("code: $code - message: ${ex?.message}")
            )
        })

    val confirmationChannel = Channel<DatabaseRestoreMessage.ConfirmRestore>()
    val async = session.async(Dispatchers.IO) {
        logger.debug("awaiting restore confirmation")
        val confirmation = confirmationChannel.receive()
        logger.info("received restore confirmation $confirmation")
        if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseRestoreMessage.start())
        downloader.restore()
    }
    logger.debug("sending confirmation request")
    if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseRestoreMessage.requestConfirm(restore.command))
    else restore.close()

    val incomingMessages = session.async(Dispatchers.IO) {
        logger.debug("start receiver")
        while (true) {
            val receive = incoming.receiveOrNull() ?: run {
                logger.debug("received null, shutting down")
                restore.close()
                return@async
            }
            if (receive.frameType == FrameType.TEXT) {
                val text = (receive as Frame.Text).readTextOrNull()
                logger.debug("text frame: $text")
                when (text) {
                    is DatabaseRestoreMessage.ConfirmRestore -> confirmationChannel.send(text)
                }
            }
        }
    }
    async.await()
    incomingMessages.await()
    logger.debug("channel closed ${session.closeReason.await()}")
}