package io.github.hoeggi.openshiftdb.api

import io.github.hoeggi.openshiftdb.api.response.LogEvent
import io.github.hoeggi.openshiftdb.server.Path
import io.github.hoeggi.openshiftdb.server.log
import io.github.hoeggi.openshiftdb.server.v1
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.WebSocketListener
import org.slf4j.LoggerFactory

interface LoggingApi {
    fun log(): Flow<List<LogEvent>>

    companion object {
        fun api(port: Int, baseUrl: BasePath): LoggingApi {
            return LoggingApiImpl("$baseUrl:$port${Path.v1().path}")
        }
    }
}

private class LoggingApiImpl(url: BasePath) : LoggingApi {

    private val client = OkHttpClient.Builder()
        .build() + url

    override fun log(): Flow<List<LogEvent>> =
        callbackFlow {
            val request = client.second.withPath(Path.log()).toGetRequest()
            val listener: WebSocketListener = createListener(this)
            val webSocket = client.first.newWebSocket(request, listener)
            awaitClose {
                logger.debug("closing flow")
            }
        } // .shareIn(CoroutineScope(coroutineContext), SharingStarted.Eagerly)

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingApiImpl::class.java)
    }
}
