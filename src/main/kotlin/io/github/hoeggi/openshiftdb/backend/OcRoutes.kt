package io.github.hoeggi.openshiftdb.backend

import io.github.hoeggi.openshiftdb.process.OC
import io.github.hoeggi.openshiftdb.process.ProcessResult
import io.github.hoeggi.openshiftdb.process.findPassword
import io.github.hoeggi.openshiftdb.process.parseServer
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

private val oc = OC()

private data class Response<T>(val data: T, val result: ProcessResult)

fun Route.oc() {
    route("/oc") {
        authenticate("postgres") {

            get("/version", version())
        }

        get("/server", server())
        get("/services", services())
        route("/projects") {
            get(projects())
            get("/current", project())
        }
        get("/password", password())
        webSocket("/port-forward", null, portForward())
    }
}

fun password(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val secret = oc.secrets()
        val username = call.request.queryParameters["username"]
        if (username == null) {
            call.respond(HttpStatusCode.BadRequest, "missing username")
        } else {
            val findPassword = findPassword(secret.json, username)
            if (findPassword.isNullOrEmpty()) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(Response(findPassword, secret.result))
            }
        }
    }

fun version(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val version = oc.version()
        call.respond(Response(version.text, version.result))
    }

fun services(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val services = oc.services()
        call.respond(Response(services.services, services.result))
    }

fun project(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val projects = oc.project()
        call.respond(Response(projects.text, projects.result))
    }

fun projects(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val projects = oc.projects()
        call.respond(Response(projects.projects, projects.result))
    }

fun server(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val listServer = oc.listServer()
        val parseServer = parseServer(listServer.json)


        call.respond(Response(parseServer, listServer.result))
    }

fun portForward(): suspend DefaultWebSocketServerSession.() -> Unit = {
    var portForward: OC.PortForward? = null
    try {
        val parameters = call.parameters
        val project = parameters["project"]
        val svc = parameters["svc"]
        val port = parameters["port"]
        if (project != null && svc != null && port != null) {
            portForward = oc.portForward(project, svc, port)
            val stream = async(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                while (true) {
                    val readUtf8Line = portForward.stream.readUtf8Line() ?: break
                    if (outgoing.isClosedForSend.not()) outgoing.send(Frame.Text("$readUtf8Line"))
                    else break
                }
            }

            val error = async(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                while (true) {
                    val readUtf8Line = portForward.errorStream.readUtf8Line() ?: break
                    if (outgoing.isClosedForSend.not()) outgoing.send(Frame.Text("$readUtf8Line"))
                    else break
                }
            }

            val incoming = async(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                while (true) {
                    val receive = incoming.receiveOrNull() ?: run {
                        portForward.stop()
                        return@async
                    }
                    if (receive.frameType == FrameType.TEXT) {
                        val text = (receive as Frame.Text).readText()
                        println("received unexpected text frame: $text")
                    }
                }
            }
            incoming.await()
            stream.await()
            error.await()
            println("channel closed ${closeReason.await()}")
        } else {
            close(CloseReason(CloseReason.Codes.GOING_AWAY, "missing parameters"))
        }
    } catch (e: ClosedReceiveChannelException) {
        println("ClosedReceiveChannelException ${closeReason.await()}")
        e.printStackTrace()
        portForward?.stop()
    } catch (e: Throwable) {
        e.printStackTrace()
        println("Throwable ${closeReason.await()}")
        portForward?.stop()
    } finally {
        portForward?.stop()
    }
}

