package io.github.hoeggi.openshiftdb.server.handler.postgres

import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.api.response.PortForwardMessage
import io.github.hoeggi.openshiftdb.oc.OC
import io.github.hoeggi.openshiftdb.oc.PortForward
import io.github.hoeggi.openshiftdb.postgres.DatabaseDownloader
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

fun PostgresDump(): suspend DefaultWebSocketServerSession.() -> Unit = {
    val logger = LoggerFactory.getLogger(this::class.java)
    try {
        val principal = call.principal<PostgresPrincibal>()
        val username = principal?.username
        val password = principal?.password

        val parameters = call.parameters
        val database = parameters["database"]
        val path = parameters["path"]
        if (database == null || principal == null || username == null || password == null || path == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {

            val dump = Postgres.dumpDatabase(database, path, username, password)
            val downloader = DatabaseDownloader(dump,
                onNewLine = {
                    logger.debug("onNewLine: $it")
                    if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseDownloadMessage.inprogress(it))
                    else dump.close()
                },
                onSuccess = {
                    logger.debug("onNewErrorLine: $it")
                    if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseDownloadMessage.finish(it))
                    else dump.close()
                },
                onError = { code, ex ->
                    logger.error("onError: $code", ex)
                    close(
                        CloseReason.Codes.INTERNAL_ERROR,
                        DatabaseDownloadMessage.error("code: $code - message: ${ex?.message}")
                    )
                })
            val async = async {
                if (outgoing.isClosedForSend.not()) outgoing.sendAsText(DatabaseDownloadMessage.start())
                downloader.download()
            }
            val incoming = async(Dispatchers.IO) {
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
            incoming.await()
            logger.debug("channel closed ${closeReason.await()}")
        }
    } catch (e: ClosedReceiveChannelException) {
        logger.debug("ClosedReceiveChannelException ${closeReason.await()}", e)
    } catch (e: Throwable) {
        logger.error("Throwable ${closeReason.await()}", e)
    }
}
