package io.github.hoeggi.openshiftdb.server.routes

import io.github.hoeggi.openshiftdb.server.*
import io.github.hoeggi.openshiftdb.server.handler.oc.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

internal fun Route.oc() {
    route(Path.oc()) {
        get {
            call.respond(HttpStatusCode.OK, "/oc")
        }
        get(Path.version(), Version())
        get(Path.server(), Cluster())
        get(Path.services(), Services())
        get(Path.context(), Context())
        post(Path.context(), SwitchContext())

        route(Path.login()) {
            get(CheckLogin())
            post(Login())
        }

        route(Path.projects()) {
            post(SwitchProject())
            get(Projects())
            get(Path.current(), Project())
        }
        route(Path.secrets()) {
            get(Path.password(), Password())
            get(Secrets())
        }
        webSocket(Path.portForward(), PortForward())
    }
}











