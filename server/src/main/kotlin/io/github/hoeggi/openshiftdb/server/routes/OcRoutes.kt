package io.github.hoeggi.openshiftdb.server.routes

import io.github.hoeggi.openshiftdb.server.handler.oc.*
import io.ktor.routing.*
import io.ktor.websocket.*

fun Route.oc() {
    route("/oc") {
        get("/version", Version())
        get("/server", Cluster())
        get("/services", Services())

        route("/login") {
            get(CheckLogin())
            post("", Login())
        }

        route("/projects") {
            post("", SwitchProject())
            get(Projects())
            get("/current", Project())
        }
        get("/password", Password())
        webSocket("/port-forward", null, PortForward())
    }
}











