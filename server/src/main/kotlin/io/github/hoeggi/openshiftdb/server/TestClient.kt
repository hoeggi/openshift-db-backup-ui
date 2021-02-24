import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString

//package io.github.hoeggi.openshiftdb.server
//
//import okhttp3.*
//import okhttp3.HttpUrl.Companion.toHttpUrl
//import okio.ByteString
//
class TestClient {
//    private val webService = OkHttpClient.Builder()
//        .addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
//            chain.proceed(
//                chain.request()
//                    .newBuilder()
//                    .header("Connection", "Upgrade")
//                    .header("Upgrade", "websocket")
//                    .build()
//            )
//        }).build()
//    val webService = HttpClient(CIO) {
//        install(WebSockets) {
//            useDefaultTransformers
//        }
//        engine {
//            preconfigured = client
//            webSocketFactory = client
//            addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
//                chain.proceed(
//                    chain.request()
//                        .newBuilder()
//                        .header("Connection", "Upgrade")
//                        .header("Upgrade", "websocket")
//                        .build()
//                )
//            })
//        }
//        engine {
//
//        }
//    }

//    private suspend fun webSocket(targetPort: Int, delay: Long, close: String, vararg messages: String) {
//        webService.ws(
//            request = {
//                method = HttpMethod.Get
//                url.apply {
//                    parameters.apply {
//                        append("project", "playground-hoeggi")
//                        append("svc", "postgres12")
//                        append("port", "$targetPort")
//                    }
//                    port = 32000
//                    host = "localhost"
//                    encodedPath = "v1/oc/port-forward"
//                    protocol = URLProtocol.HTTP
//                }
//                headers.apply {
//                    append("Connection", "Upgrade")
//                    append("Upgrade", "websocket")
//                }
//            }, block = session(delay, close, *messages)
//        )
//    }
//
//    private suspend fun session(
//        delay: Long,
//        close: String,
//        vararg messages: String
//    ): suspend DefaultClientWebSocketSession.() -> Unit =
//        {
//            withContext(Dispatchers.Default) {
//                while (isActive) {
//                    when (val frame = incoming.receiveOrNull()) {
//                        is Frame.Text -> println(frame.readText())
//                        is Frame.Binary -> println(frame.readBytes())
//                    }
//                }
//            }
//            withContext(Dispatchers.Default) {
//                for (message in messages) {
//                    delay(delay)
//                    send(Frame.Text(message))
//                }
//                delay(delay)
//                close(CloseReason(CloseReason.Codes.NORMAL, close))
//            }
//        }

    val client = OkHttpClient.Builder()
        .build()

    fun webSocket(port: Int) = client.newWebSocket(
        Request.Builder()
            .header("Connection", "Upgrade")
            .header("Upgrade", "websocket")
            .url(
                "http://localhost:32000/v1/oc/port-forward".toHttpUrl()
                    .newBuilder()
                    .addQueryParameter("project", "playground-hoeggi")
                    .addQueryParameter("svc", "postgres12")
                    .addQueryParameter("port", "$port")
                    .build()
            )
            .build(),
        object : WebSocketListener() {
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("onClosed - $code - $reason")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("onClosing - $code - $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("onMessage - $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("onMessage - ${bytes.utf8()}")
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("onOpen")
            }
        }
    )

    val version
        get() = client.newCall(
            Request.Builder()
                .header("Connection", "Upgrade")
                .header("Upgrade", "websocket")
                .url(
                    "http://localhost:32000/v1/oc/version".toHttpUrl()
                )
                .build()
        )

    val switchProject
        get() = client.newCall(
            Request.Builder()
                .url(
                    "http://localhost:32000/v1/oc/projects"
                )
                .post(
//                    Json.encodeToString(ProjectApi("playground-daniel"))
                    "".toRequestBody("application/json".toMediaType())
                )
                .build()
        )

    //
//    val versionBasicAuth
//        get() = client.newCall(
//            Request.Builder()
//                .header("Connection", "Upgrade")
//                .header("Upgrade", "websocket")
//                .header("Authorization", Credentials.basic("postgres", "5xhdr;T,D+Fs6NE})mJ:{Fsw"))
//                .url(
//                    "http://localhost:32000/v1/oc/version".toHttpUrl()
//                )
//                .build()
//        )
//

    fun runSwitchProject() {
//        Thread.sleep(2000)
        val execute = switchProject.execute()
        println(execute)
        val body = execute.body
        println(body)
        val response = body?.string()
        println(response)
    }

    fun runVersion() {
        Thread.sleep(2000)
        val execute = version.execute()
        println(execute)
        val body = execute.body
        println(body)
        val response = body?.string()
        println(response)
    }

    //
//    fun runVersionBasicAuth() {
//        Thread.sleep(2000)
//        val execute = versionBasicAuth.execute()
//        println(execute)
//        Thread.sleep(2000)
//    }
//
    fun runWebsocket() {
        val socket = webSocket(5432)
        Thread.sleep(3000)
        socket.send("test message")
        Thread.sleep(3000)
        socket.close(1000, "we are done")
        println("closed")
    }
}