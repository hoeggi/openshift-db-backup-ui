package io.github.hoeggi.openshiftdb.api

import io.github.hoeggi.openshiftdb.api.response.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.WebSocketListener
import org.slf4j.LoggerFactory
import kotlin.coroutines.coroutineContext

interface PostgresApi {

    enum class DatabaseViewFormat(val view: String) {
        Table("table"), Text("text"), List("list")
    }

    suspend fun restoreInfo(path: String): Result<RestoreInfoApi>
    suspend fun restoreCommand(user: String, password: String, path: String): Result<RestoreCommandApi>
    suspend fun restoreDatabase(
        username: String,
        password: String,
        path: String,
        database: String,
        exists: Boolean,
        confirmationChannel: ReceiveChannel<Boolean>,
    ): Flow<DatabaseRestoreMessage>

    suspend fun toolsVersion(): Result<ToolsVersionApi>
    suspend fun databaseVersion(username: String, password: String): Result<DatabaseVersionApi>
    suspend fun databases(
        username: String,
        password: String,
        format: DatabaseViewFormat? = null,
    ): Result<DatabasesApi>

    suspend fun defaultDatabases(username: String, password: String): Result<DefaultDatabaseApi>

    suspend fun dumpDatabases(
        username: String,
        password: String,
        database: String,
        path: String,
        format: String = "custom",
    ): Flow<DatabaseDownloadMessage>

    companion object {
        fun api(port: Int, baseUrl: BasePath): PostgresApi =
            PostgresApiImpl("$baseUrl:$port/v1/postgres/")
    }

}

private class PostgresApiImpl(url: BasePath) : PostgresApi {

    private val client = OkHttpClient.Builder()
        .build() + url

    override suspend fun restoreInfo(path: String): Result<RestoreInfoApi> =
        withContext(Dispatchers.IO) {
            client.first.newCall(
                client.second.withPath("restore/info")
                    .withQuery("path" to path)
                    .toGetRequest()
            ).execute().get()
        }

    override suspend fun restoreCommand(user: String, password: String, path: String): Result<RestoreCommandApi> =
        withContext(Dispatchers.IO) {
            client.first.newCall(
                client.second.withPath("restore/command")
                    .withQuery("path" to path)
                    .toGetRequest(Credentials.basic(user, password))
            ).execute().get()
        }

    override suspend fun restoreDatabase(
        username: String,
        password: String,
        path: String,
        database: String,
        exists: Boolean,
        confirmationChannel: ReceiveChannel<Boolean>,
    ): Flow<DatabaseRestoreMessage> = callbackFlow {
        val request = client.second.withPath("restore")
            .withQuery(
                "database" to database,
                "exists" to "$exists",
                "path" to path
            )
            .toGetRequest(Credentials.basic(username, password))
        val serializer = DatabaseRestoreMessage.serializer()
        val listener: WebSocketListener = createListener(this, serializer)
        val webSocket = client.first.newWebSocket(request, listener)

        launch {
            logger.debug("awaiting restore confirmation")
            val receive = confirmationChannel.receive()
            logger.debug("received restore confirmation: $receive")
            if (receive) webSocket.send(Json.encodeToString(DatabaseRestoreMessage.confirm()))
            else webSocket.close(1000, Json.encodeToString(DatabaseRestoreMessage.finish()))
        }
        awaitClose {
            logger.debug("closing flow")
            webSocket.close(1000, Json.encodeToString(DatabaseRestoreMessage.finish()))
        }
    }.shareIn(CoroutineScope(coroutineContext), SharingStarted.Eagerly)

    override suspend fun toolsVersion(): Result<ToolsVersionApi> =
        withContext(Dispatchers.IO) { client.get("version/tools") }

    override suspend fun databaseVersion(username: String, password: String): Result<DatabaseVersionApi> =
        withContext(Dispatchers.IO) {
            client.get("version/database", Credentials.basic(username, password))
        }


    override suspend fun databases(
        username: String,
        password: String,
        format: PostgresApi.DatabaseViewFormat?,
    ): Result<DatabasesApi> = withContext(Dispatchers.IO) {
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
    }

    override suspend fun defaultDatabases(
        username: String,
        password: String,
    ): Result<DefaultDatabaseApi> = withContext(Dispatchers.IO) {
        client.get("databases/default", Credentials.basic(username, password))
    }

    override suspend fun dumpDatabases(
        username: String,
        password: String,
        database: String,
        path: String,
        format: String,
    ): Flow<DatabaseDownloadMessage> = callbackFlow {
        val request = client.second.withPath("databases/dump")
            .withQuery(
                "database" to database,
                "path" to path,
                "format" to format
            ).toGetRequest(Credentials.basic(username, password))
        val serializer = DatabaseDownloadMessage.serializer()
        val listener: WebSocketListener = createListener(this, serializer)
        val webSocket = client.first.newWebSocket(request, listener)
        awaitClose {
            logger.debug("closing flow")
            webSocket.close(1000, Json.encodeToString(DatabaseDownloadMessage.finish("closing")))
        }
    }.shareIn(CoroutineScope(coroutineContext), SharingStarted.Eagerly)


    companion object {
        val logger = LoggerFactory.getLogger(PostgresApiImpl::class.java)
    }
}