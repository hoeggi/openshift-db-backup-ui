package io.github.hoeggi.openshiftdb.api

import io.github.hoeggi.openshiftdb.api.response.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import org.slf4j.LoggerFactory
import java.io.IOException
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
    suspend fun secrets(): Result<List<SecretsApi>>

    suspend fun portForward(
        project: String,
        svc: String,
        port: Int,
    ): Flow<PortForwardMessage>

    companion object {
        fun api(port: Int, baseUrl: BasePath): OcApi =
            OcApiImpl("$baseUrl:$port/v1/oc/")
    }
}

private class OcApiImpl(url: BasePath) : OcApi {

    private val client = OkHttpClient.Builder()
        .build() + url

    override suspend fun version(): Result<VersionApi> =
        withContext(Dispatchers.IO) { client.get("version") }

    override suspend fun server(): Result<List<ClusterApi>> =
        withContext(Dispatchers.IO) { client.get("server") }

    override suspend fun projects(): Result<List<ProjectApi>> =
        withContext(Dispatchers.IO) { client.get("projects") }

    override suspend fun currentProject(): Result<ProjectApi> =
        withContext(Dispatchers.IO) { client.get("projects/current") }

    override suspend fun switchProject(project: String) = switchProject(ProjectApi(project))
    override suspend fun switchProject(project: ProjectApi): Result<ProjectApi> =
        withContext(Dispatchers.IO) { client.post("projects", project) }

    override suspend fun secrets(): Result<List<SecretsApi>> =
        withContext(Dispatchers.IO) { client.get("secrets") }

    override suspend fun services(): Result<List<ServicesApi>> =
        withContext(Dispatchers.IO) { client.get("services") }

    override suspend fun password(username: String): Result<String> =
        withContext(Dispatchers.IO) {
            client.first.newCall(
                client.second.withPath("secrets/password")
                    .withQuery("username" to username)
                    .toGetRequest()
            ).execute().get()
        }

    override suspend fun login(token: String, server: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            authorize(
                loginRequest().toPostRequest(LoginApi(token, server))
            )
        }

    override suspend fun checkLogin(): Result<Unit> =
        withContext(Dispatchers.IO) { authorize(loginRequest().toGetRequest()) }


    private fun loginRequest() = client.second.withPath("login")
    private fun authorize(request: Request): Result<Unit> = try {
        val result = client.first.newCall(request).execute()
        when (result.code) {
            204 -> Result.success(Unit)
            else -> Result.failure<Unit>(RuntimeException("Unauthorized"))
        }
    } catch (ex: IOException) {
        logger.error("unable to reach api", ex)
        Result.failure(RuntimeException("Unauthorized"))
    }

    override suspend fun portForward(project: String, svc: String, port: Int) =
        callbackFlow {
            val request = client.second.withPath("port-forward")
                .withQuery(
                    "project" to project,
                    "svc" to svc,
                    "port" to "$port"
                ).toGetRequest()
            val serializer = PortForwardMessage.serializer()
            val listener: WebSocketListener = createListener(this, serializer)
            val webSocket = client.first.newWebSocket(request, listener)
            awaitClose {
                logger.debug("closing flow")
                webSocket.close(1000, Json.encodeToString(PortForwardMessage.close("closing")))
            }
        }.shareIn(CoroutineScope(coroutineContext), SharingStarted.Eagerly)

    companion object {
        val logger = LoggerFactory.getLogger(OcApiImpl::class.java)
    }
}