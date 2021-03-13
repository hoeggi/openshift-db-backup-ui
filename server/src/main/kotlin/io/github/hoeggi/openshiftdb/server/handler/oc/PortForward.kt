package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.PortForwardMessage
import io.github.hoeggi.openshiftdb.oc.OC
import io.github.hoeggi.openshiftdb.oc.PortForward
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private suspend fun SendChannel<Frame>.sendAsText(message: PortForwardMessage) {
    send(Frame.Text(Json.encodeToString(message)))
}

private suspend fun WebSocketSession.close(code: CloseReason.Codes, message: PortForwardMessage) {
    close(CloseReason(code, Json.encodeToString(message)))
}

internal fun PortForward(): suspend DefaultWebSocketServerSession.() -> Unit = {
    val logger = LoggerFactory.getLogger(this::class.java)
    var portForward: PortForward? = null
    try {
        val parameters = call.parameters
        val project = parameters["project"]
        val svc = parameters["svc"]
        val port = parameters["port"]
        if (project != null && svc != null && port != null) {

            portForward = PortForward(
                onNewLine = {
                    logger.debug("onNewLine: $it")
                    if (outgoing.isClosedForSend.not()) outgoing.sendAsText(PortForwardMessage.message(it))
                    else portForward?.stop()
                },
                onNewErrorLine = {
                    logger.debug("onNewErrorLine: $it")
                    if (outgoing.isClosedForSend.not()) outgoing.sendAsText(PortForwardMessage.error(it))
                    else portForward?.stop()
                },
                onClosed = {
                    logger.debug("onClosed: $it")
                    close(CloseReason.Codes.NORMAL, PortForwardMessage.close(it))
                }
            )
            val target = OC.PortForwardTarget(project, svc, port)

            val open = async(Dispatchers.IO) { portForward.open(target) }
            val incoming = async(Dispatchers.IO) {
                logger.debug("start receiver")
                while (true) {
                    val receive = incoming.receiveOrNull() ?: run {
                        logger.debug("received null, shutting down")
                        portForward.stop()
                        return@async
                    }
                    if (receive.frameType == FrameType.TEXT) {
                        val text = (receive as Frame.Text).readText()
                        logger.debug("received unexpected text frame: $text")
                    }
                }
            }
            open.await()
            incoming.await()
            logger.debug("channel closed ${closeReason.await()}")
        } else {
            close(CloseReason.Codes.CANNOT_ACCEPT, PortForwardMessage.close("missing parameters"))
        }
    } catch (e: ClosedReceiveChannelException) {
        logger.debug("ClosedReceiveChannelException ${closeReason.await()}", e)
    } catch (e: Throwable) {
        logger.error("Throwable ${closeReason.await()}", e)
    } finally {
        portForward?.stop()
    }
}
