package io.github.hoeggi.openshiftdb.server

import io.github.hoeggi.openshiftdb.api.*
import io.github.hoeggi.openshiftdb.process.OC
import io.github.hoeggi.openshiftdb.process.PortForward
import io.github.hoeggi.openshiftdb.process.ProcessResult
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.serialization.Serializable

private val oc = OC()

@Serializable
private data class Response<T>(val data: T, val result: Int)

fun Route.oc() {
    route("/oc") {
        get("/version", version())
        get("/server", server())
        get("/services", services())

        route("/projects") {
            post("", switchProject())
            get(projects())
            get("/current", project())
        }
        get("/password", password())
        webSocket("/port-forward", null, portForward())
    }
}

//.(ProjectApi)
fun switchProject(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val project = call.receiveOrNull<ProjectApi>()
        val projectName = project?.name
        if (projectName == null) {
            call.respond(HttpStatusCode.BadRequest, "missing projectName")
        } else {
            val switchProject = oc.switchProject(projectName)
            call.respond(Response(ProjectApi(switchProject.text), switchProject.result.code))
        }
    }

fun project(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val projects = oc.project()
        call.respond(Response(ProjectApi(projects.text), projects.result.code))
    }

fun projects(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val projects = oc.projects()
        call.respond(Response(projects.projects.map { ProjectApi(it) }, projects.result.code))
    }

fun password(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val username = call.request.queryParameters["username"]
        if (username == null) {
            call.respond(HttpStatusCode.BadRequest, "missing username")
        } else {
            val secret = oc.secrets(username)
            if (secret.password.isNullOrEmpty()) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(Response(secret.password, secret.result.code))
            }
        }
    }

fun version(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val version = oc.version()
        version.version?.run {
            call.respond(
                Response(
                    VersionApi(
                        oc = releaseClientVersion,
                        openshift = openshiftVersion,
                        kubernets = serverVersion.gitVersion
                    ),
                    result = version.result.code
                )
            )
        } ?: call.respond(HttpStatusCode.NotFound)
    }


fun server(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val listServer = oc.listServer()
        if (listServer.server.isEmpty()) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(
                Response(
                    listServer.server.map {
                        ClusterApi(
                            name = it.name,
                            server = it.cluster.server

                        )
                    },
                    result = listServer.result.code
                )
            )
        }
    }

fun services(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val services = oc.services()
        if (services.services.isEmpty()) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(
                Response(
                    services.services.map {
                        ServicesApi(
                            name = it.name,
                            ports = it.ports.map {
                                ServicesPortApi(
                                    port = it.port,
                                    targetPort = it.targetPort,
                                    protocol = it.protocol
                                )
                            }
                        )
                    },
                    result = services.result.code
                )
            )
        }
        call.respond(Response(services.services, services.result.code))
    }


fun portForward(): suspend DefaultWebSocketServerSession.() -> Unit = {
    var portForward: PortForward? = null
    try {
        val parameters = call.parameters
        val project = parameters["project"]
        val svc = parameters["svc"]
        val port = parameters["port"]
        if (project != null && svc != null && port != null) {

            portForward = PortForward(
                onNewLine = {
                    if (outgoing.isClosedForSend.not()) outgoing.send(Frame.Text("$it"))
                    else portForward?.stop()
                },
                onNewErrorLine = {
                    if (outgoing.isClosedForSend.not()) outgoing.send(Frame.Text("$it"))
                    else portForward?.stop()
                }
            )
            val target = OC.PortForwardTarget(project, svc, port)

            val open = async(Dispatchers.IO) { portForward.open(target) }
            val incoming = async(Dispatchers.IO) {
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
            open.await()
            incoming.await()
            portForward.stop()
            println("channel closed ${closeReason.await()}")
        } else {
            close(CloseReason(CloseReason.Codes.GOING_AWAY, "missing parameters"))
        }
    } catch (e: ClosedReceiveChannelException) {
        println("ClosedReceiveChannelException ${closeReason.await()}")
        e.printStackTrace()
    } catch (e: Throwable) {
        e.printStackTrace()
        println("Throwable ${closeReason.await()}")
    } finally {
        portForward?.stop()
    }
}

