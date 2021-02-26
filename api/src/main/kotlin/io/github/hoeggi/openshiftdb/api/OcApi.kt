package io.github.hoeggi.openshiftdb.api

import io.github.hoeggi.openshiftdb.api.response.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import org.slf4j.LoggerFactory
import java.io.Closeable
import kotlin.coroutines.coroutineContext

interface OcApi {

    suspend fun version(): Result<VersionApi>
    suspend fun server(): Result<List<ClusterApi>>
    suspend fun projects(): Result<List<ProjectApi>>
    suspend fun switchProject(project: ProjectApi): Result<ProjectApi>
    suspend fun switchProject(project: String): Result<ProjectApi>
    suspend fun currentProject(): Result<ProjectApi>
    suspend fun password(username: String): Result<String>
    suspend fun services(): Result<List<ServicesApi>>
    suspend fun login(token: String, server: String): Result<Unit>
    suspend fun checkLogin(): Result<Unit>

    suspend fun portForward(
        project: String,
        svc: String,
        port: Int
    ): Flow<PortForwardMessage>

    companion object {
        fun api(port: Int, baseUrl: BasePath): OcApi =
            OcApiImpl("$baseUrl:$port/v1/oc/")
    }
}

private class OcApiImpl(url: BasePath) : OcApi {

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

    override suspend fun version(): Result<VersionApi> = client.get("version")

    override suspend fun server(): Result<List<ClusterApi>> = client.get("server")

    override suspend fun projects(): Result<List<ProjectApi>> = client.get("projects")
    override suspend fun currentProject(): Result<ProjectApi> = client.get("projects/current")

    override suspend fun switchProject(project: ProjectApi): Result<ProjectApi> = client.post("projects", project)

    override suspend fun switchProject(project: String) = switchProject(ProjectApi(project))

    override suspend fun password(username: String): Result<String> =
        client.first.newCall(
            client.second.withPath("password")
                .withQuery("username" to username)
                .toGetRequest()
        )
            .execute().get()

    override suspend fun services(): Result<List<ServicesApi>> = client.get("services")

    override suspend fun login(token: String, server: String): Result<Unit> {
        return authorize(
            client.second.withPath("login").toPostRequest(LoginApi(token, server))
        )
    }

    override suspend fun checkLogin(): Result<Unit> {
        return authorize(
            client.second.withPath("login").toGetRequest()
        )
    }

    private fun authorize(request: Request): Result<Unit> {
        val result = client.first.newCall(request).execute()
        return when (result.code) {
            204 -> Result.success(Unit)
            else -> Result.failure(RuntimeException("Unauthorized"))
        }
    }

    override suspend fun portForward(project: String, svc: String, port: Int) = callbackFlow {
        val request = webSocketClient.second.withPath("port-forward")
            .withQuery(
                "project" to project,
                "svc" to svc,
                "port" to "$port"
            ).toGetRequest()

        val listener: WebSocketListener = createListener(this@callbackFlow)
        val webSocket = webSocketClient.first.newWebSocket(request, listener)
        awaitClose {
            logger.debug("closing flow")
            webSocket.close(1000, Json.encodeToString(PortForwardMessage.close("closing")))
        }
    }.shareIn(CoroutineScope(coroutineContext), SharingStarted.Eagerly)


    private fun createListener(producer: ProducerScope<PortForwardMessage>): WebSocketListener {
        return object : WebSocketListener() {

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                logger.debug("onClosed - $code - $reason")
                val message = Json.decodeFromString<PortForwardMessage>(reason)
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
                val message = Json.decodeFromString<PortForwardMessage>(text)
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
        val logger = LoggerFactory.getLogger(OcApiImpl::class.java)
    }
}