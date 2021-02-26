//import io.github.hoeggi.openshiftdb.api.Api
//import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
//import io.github.hoeggi.openshiftdb.api.response.PortForwardMessage
//import io.github.hoeggi.openshiftdb.server.logging
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.*
//import okhttp3.*
//import okhttp3.HttpUrl.Companion.toHttpUrl
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody.Companion.toRequestBody
//import okio.ByteString
//import java.util.concurrent.Executors
//import kotlin.coroutines.coroutineContext
//
////package io.github.hoeggi.openshiftdb.server
////
////import okhttp3.*
////import okhttp3.HttpUrl.Companion.toHttpUrl
////import okio.ByteString
////
//
//
////suspend inline fun <reified T> StateFlow<Any>.cast(defaultValue: T): StateFlow<T> {
////    val stream = MutableStateFlow(defaultValue).stateIn()
////    collect {
////        println("collected: $it")
////        if (it is T) stream.value = it else stream.value = defaultValue
////    }
////    return stream
//////    println("cast: ${this.value}")
//////    return if (this.value is T) {
//////        this as StateFlow<T>
//////    } else {
//////        MutableStateFlow(defaultValue).asStateFlow()
//////    }
////}
////
////class CastFlow<T>(defaultValue: T, upstream: StateFlow<Any>) : StateFlow<T> by upstream {
////
////}
//
//class TestClient {
//    fun test(port: Int) {
//
////        GlobalScope.launch {
////            val list = listOf("one", "two", "three")
////            val test: MutableStateFlow<Any> = MutableStateFlow(Unit)
////
////            val async = async {
////                test.filterIsInstance<String>().stateIn(this)
////                    .collect {
////                        println("$it")
////                    }
////            }
////
////            val async1 = async {
////                for (step in list) {
////                    delay(1000)
////                    println("emitting: $step")
////                    test.value = step
////                }
////            }
////            awaitAll(async, async1)
////        }
//
//
////        val build = "http://example.com/v1".toHttpUrl().newBuilder()
////            .addEncodedPathSegments("test/test")
////            .addPathSegments("test2/test2").build()
////        val toString = build.toString()
////        Executors.newSingleThreadExecutor().execute {
////            GlobalScope.launch(newFixedThreadPoolContext(4, "test")) {
////                logging.debug("waiting for server")
////                delay(3000)
////                logging.debug("making api request")
////                val api = Api(port)
////
////                val async = async(Dispatchers.IO + CoroutineName("pf")) {
////                    val response: Flow<PortForwardMessage> = api.portForward("playground-hoeggi", "postgres12", "5432")
////                    response.collect {
////                        ensureActive()
////                        logging.debug("message from port-forward: $it")
////                    }
////                }
////                val pw = async(Dispatchers.IO + CoroutineName("pw")) {
////                    api.password("postgres")
////                }.await()
////                logging.debug("$pw")
////                delay(3000)
////                val version = api.version()
////                logging.debug("$version")
////                val defaultDatabases = api.defaultDatabases("postgres", pw.getOrNull() ?: "")
////                logging.debug("$defaultDatabases")
//////            val databasestable = api.databases("postgres", pw.getOrNull() ?: "", PostgresApi.DatabaseViewFormat.Table)
//////            logging.debug("$databasestable")
//////            delay(1000)
//////            val databasessingle = api.databases("postgres", pw.getOrNull() ?: "", PostgresApi.DatabaseViewFormat.Text)
//////            logging.debug("$databasessingle")
//////            delay(1000)
//////            val databases = api.databases("postgres", pw.getOrNull() ?: "")
//////            logging.debug("$databases")
//////            delay(1000)
//////            val databaseslist = api.databases("postgres", pw.getOrNull() ?: "", PostgresApi.DatabaseViewFormat.List)
//////            logging.debug("$databaseslist")
//////            delay(1000)
//////
////                val dump = async(Dispatchers.IO + CoroutineName("pf")) {
////                    val dumpDatabases = api.dumpDatabases(
////                        "postgres",
////                        pw.getOrNull() ?: "",
////                        "postgres",
////                        "/home/hoeggi/repos/name-that-color-desktop/server"
////                    )
////                    dumpDatabases.collect {
////                        ensureActive()
////                        logging.debug("---------$it---------")
////                        if (it is DatabaseDownloadMessage.FinishMessage || it is DatabaseDownloadMessage.ErrorMessage) {
////                            cancel()
////                        }
////                    }
////                }.await()
////
////                async.cancel()
////            }
////        }
//    }
//
//    val client = OkHttpClient.Builder()
//        .build()
//
//    fun webSocket(port: Int) = client.newWebSocket(
//        Request.Builder()
//            .header("Connection", "Upgrade")
//            .header("Upgrade", "websocket")
//            .url(
//                "http://localhost:32000/v1/oc/port-forward".toHttpUrl()
//                    .newBuilder()
//                    .addQueryParameter("project", "playground-hoeggi")
//                    .addQueryParameter("svc", "postgres12")
//                    .addQueryParameter("port", "$port")
//                    .build()
//            )
//            .build(),
//        object : WebSocketListener() {
//            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//                println("onClosed - $code - $reason")
//            }
//
//            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
//                println("onClosing - $code - $reason")
//            }
//
//            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                t.printStackTrace()
//            }
//
//            override fun onMessage(webSocket: WebSocket, text: String) {
//                println("onMessage - $text")
//            }
//
//            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//                println("onMessage - ${bytes.utf8()}")
//            }
//
//            override fun onOpen(webSocket: WebSocket, response: Response) {
//                println("onOpen")
//            }
//        }
//    )
//
//    val version
//        get() = client.newCall(
//            Request.Builder()
//                .header("Connection", "Upgrade")
//                .header("Upgrade", "websocket")
//                .url(
//                    "http://localhost:32000/v1/oc/version".toHttpUrl()
//                )
//                .build()
//        )
//
//    val switchProject
//        get() = client.newCall(
//            Request.Builder()
//                .url(
//                    "http://localhost:32000/v1/oc/projects"
//                )
//                .post(
////                    Json.encodeToString(ProjectApi("playground-daniel"))
//                    "".toRequestBody("application/json".toMediaType())
//                )
//                .build()
//        )
//
//    //
////    val versionBasicAuth
////        get() = client.newCall(
////            Request.Builder()
////                .header("Connection", "Upgrade")
////                .header("Upgrade", "websocket")
////                .header("Authorization", Credentials.basic("postgres", "5xhdr;T,D+Fs6NE})mJ:{Fsw"))
////                .url(
////                    "http://localhost:32000/v1/oc/version".toHttpUrl()
////                )
////                .build()
////        )
////
//
//    fun runSwitchProject() {
////        Thread.sleep(2000)
//        val execute = switchProject.execute()
//        println(execute)
//        val body = execute.body
//        println(body)
//        val response = body?.string()
//        println(response)
//    }
//
//    fun runVersion() {
//        Thread.sleep(2000)
//        val execute = version.execute()
//        println(execute)
//        val body = execute.body
//        println(body)
//        val response = body?.string()
//        println(response)
//    }
//
//    //
////    fun runVersionBasicAuth() {
////        Thread.sleep(2000)
////        val execute = versionBasicAuth.execute()
////        println(execute)
////        Thread.sleep(2000)
////    }
////
//    fun runWebsocket() {
//        val socket = webSocket(5432)
//        Thread.sleep(3000)
//        socket.send("test message")
//        Thread.sleep(3000)
//        socket.close(1000, "we are done")
//        println("closed")
//    }
//}