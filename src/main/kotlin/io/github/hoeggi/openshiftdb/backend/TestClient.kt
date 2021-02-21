package io.github.hoeggi.openshiftdb.backend

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString

class TestClient {
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
//                    .newBuilder()
//                    .addQueryParameter("project", "playground-hoeggi")
//                    .addQueryParameter("svc", "postgres12")
//                    .addQueryParameter("port", "5432")
//                    .build()
                )
                .build()
        )

    val versionBasicAuth
        get() = client.newCall(
            Request.Builder()
                .header("Connection", "Upgrade")
                .header("Upgrade", "websocket")
                .header("Authorization", Credentials.basic("postgres", "5xhdr;T,D+Fs6NE})mJ:{Fsw"))
                .url(
                    "http://localhost:32000/v1/oc/version".toHttpUrl()
//                    .newBuilder()
//                    .addQueryParameter("project", "playground-hoeggi")
//                    .addQueryParameter("svc", "postgres12")
//                    .addQueryParameter("port", "5432")
//                    .build()
                )
                .build()
        )

    fun runVersion() {
        Thread.sleep(2000)
        val execute = version.execute()
        println(execute)
        Thread.sleep(2000)
    }

    fun runVersionBasicAuth() {
        Thread.sleep(2000)
        val execute = versionBasicAuth.execute()
        println(execute)
        Thread.sleep(2000)
    }

    fun runWebsocket() {
        val socket = webSocket(5432)
        val socket2 = webSocket(2022)
        Thread.sleep(2000)
        socket.send("socket test message")
        socket2.send("socket2 test message")
        Thread.sleep(2000)
        socket.close(1000, "socket we are done")
        socket2.close(1000, "socket2 we are done")
        Thread.sleep(5000)
    }
}