package io.github.hoeggi.openshiftdb.api

import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.serialization.KSerializer
import io.github.hoeggi.openshiftdb.api.response.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("io.github.hoeggi.openshiftdb.api.WebsocketListener")

internal fun <T> createListener(producer: ProducerScope<T>, serializer: KSerializer<T>): WebSocketListener {
    return object : WebSocketListener() {

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            logger.debug("onClosed - $code - $reason")
            val message = Json.decodeFromString(serializer, reason)
            if (!producer.isClosedForSend) producer.sendBlocking(message)
            producer.cancel()
            logger.debug("onClosed emit result: $message")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            logger.debug("onClosing - $code - $reason")
            val message = Json.decodeFromString(serializer, reason)
            if (!producer.isClosedForSend) producer.sendBlocking(message)
            producer.cancel()
            logger.debug("onClosing emit result: $message")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            logger.error("websocket failed, $response", t)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            logger.debug("onMessage - $text")
            val message = Json.decodeFromString(serializer, text)
            if (!producer.isClosedForSend) producer.sendBlocking(message)
            logger.debug("onMessage emit result: $message")
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            logger.debug("recived bytes instead of text - ${bytes.utf8()}")
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            logger.debug("onOpen")
        }
    }
}