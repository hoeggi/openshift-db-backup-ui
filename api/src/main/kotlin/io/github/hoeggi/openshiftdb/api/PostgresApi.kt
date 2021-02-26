package io.github.hoeggi.openshiftdb.api

import io.github.hoeggi.openshiftdb.api.response.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import org.slf4j.LoggerFactory
import kotlin.coroutines.coroutineContext

interface PostgresApi {

    enum class DatabaseViewFormat(val view: String) {
        Table("table"), Text("text"), List("list")
    }

    suspend fun toolsVersion(): Result<ToolsVersionApi>
    suspend fun databaseVersion(username: String, password: String): Result<DatabaseVersionApi>
    suspend fun databases(
        username: String,
        password: String,
        format: DatabaseViewFormat? = null
    ): Result<DatabasesApi>

    suspend fun defaultDatabases(username: String, password: String): Result<DefaultDatabaseApi>

    suspend fun dumpDatabases(
        username: String,
        password: String,
        database: String,
        path: String
    ): Flow<DatabaseDownloadMessage>

    companion object {
        fun api(port: Int, baseUrl: BasePath): PostgresApi =
            PostgresApiImpl("$baseUrl:$port/v1/postgres/")
    }

}

private class PostgresApiImpl(url: BasePath) : PostgresApi {

    private val client = OkHttpClient.Builder()
        .build() + url

    private val webSocketClient = OkHttpClient.Builder()
        .addNetworkInterceptor {
            it.proceed(
                it.request().newBuilder()
                    .header("Connection", "Upgrade")
                    .header("Upgrade", "websocket")
                    .build()
            )
        }
        .build() + url

    override suspend fun toolsVersion(): Result<ToolsVersionApi> = client.get("version/tools")

    override suspend fun databaseVersion(username: String, password: String): Result<DatabaseVersionApi> =
        client.get("version/database", Credentials.basic(username, password))

    override suspend fun databases(
        username: String,
        password: String,
        format: PostgresApi.DatabaseViewFormat?
    ): Result<DatabasesApi> =
        client.first.newCall(
            client.second.withPath("databases")
                .newBuilder()
                .apply {
                    if (format != null) {
                        withQuery("format" to format.view)
                    }
                }
                .build()
                .toGetRequest(Credentials.basic(username, password)))
            .execute().get()

    override suspend fun defaultDatabases(
        username: String,
        password: String
    ): Result<DefaultDatabaseApi> = client.get("databases/default", Credentials.basic(username, password))

    override suspend fun dumpDatabases(
        username: String,
        password: String,
        database: String,
        path: String
    ): Flow<DatabaseDownloadMessage> = callbackFlow {
        val request = webSocketClient.second.withPath("databases/dump")
            .withQuery(
                "database" to database,
                "path" to path,
            ).toGetRequest(Credentials.basic(username, password))

        val listener: WebSocketListener = createListener(this@callbackFlow)
        val webSocket = webSocketClient.first.newWebSocket(request, listener)
        awaitClose {
            logger.debug("closing flow")
            webSocket.close(1000, Json.encodeToString(DatabaseDownloadMessage.finish("closing")))
        }
    }.shareIn(CoroutineScope(coroutineContext), SharingStarted.Eagerly)

    private fun createListener(producer: ProducerScope<DatabaseDownloadMessage>): WebSocketListener {
        return object : WebSocketListener() {

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                logger.debug("onClosed - $code - $reason")
                val message = Json.decodeFromString<DatabaseDownloadMessage>(reason)
                if (!producer.isClosedForSend) producer.sendBlocking(message)
                logger.debug("onClosed emit result: $message")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                logger.debug("onClosing - $code - $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                logger.debug("onMessage - $text")
                val message = Json.decodeFromString<DatabaseDownloadMessage>(text)
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

    companion object {
        val logger = LoggerFactory.getLogger(PostgresApiImpl::class.java)
    }
}