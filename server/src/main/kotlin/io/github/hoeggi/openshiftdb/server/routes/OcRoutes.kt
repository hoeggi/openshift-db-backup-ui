package io.github.hoeggi.openshiftdb.server.routes

import io.github.hoeggi.openshiftdb.server.handler.oc.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Route::class.java)
fun Route.oc() {
    route("/oc") {
        get {
            call.respond(HttpStatusCode.OK, "/oc")
        }
        get("/version", Version())
        get("/server", Cluster())
        get("/services", Services())
        get("/context", Context())
        post("/context", SwitchContext())

        route("/login") {
            get(CheckLogin())
            post(Login())
        }

        route("/projects") {
            post("", SwitchProject())
            get("", Projects())
            get("/current", Project())
        }
        route("/secrets") {
            get("/password", Password())
            get(Secrets())
        }
        webSocket("/port-forward", null, PortForward())
    }
}











