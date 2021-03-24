package io.github.hoeggi.openshiftdb.server.routes

import io.github.hoeggi.openshiftdb.server.Path
import io.github.hoeggi.openshiftdb.server.context
import io.github.hoeggi.openshiftdb.server.current
import io.github.hoeggi.openshiftdb.server.get
import io.github.hoeggi.openshiftdb.server.handler.oc.CheckLogin
import io.github.hoeggi.openshiftdb.server.handler.oc.Cluster
import io.github.hoeggi.openshiftdb.server.handler.oc.Context
import io.github.hoeggi.openshiftdb.server.handler.oc.Login
import io.github.hoeggi.openshiftdb.server.handler.oc.Password
import io.github.hoeggi.openshiftdb.server.handler.oc.PortForward
import io.github.hoeggi.openshiftdb.server.handler.oc.Project
import io.github.hoeggi.openshiftdb.server.handler.oc.Projects
import io.github.hoeggi.openshiftdb.server.handler.oc.Secrets
import io.github.hoeggi.openshiftdb.server.handler.oc.Services
import io.github.hoeggi.openshiftdb.server.handler.oc.SwitchContext
import io.github.hoeggi.openshiftdb.server.handler.oc.SwitchProject
import io.github.hoeggi.openshiftdb.server.handler.oc.Version
import io.github.hoeggi.openshiftdb.server.login
import io.github.hoeggi.openshiftdb.server.oc
import io.github.hoeggi.openshiftdb.server.password
import io.github.hoeggi.openshiftdb.server.portForward
import io.github.hoeggi.openshiftdb.server.post
import io.github.hoeggi.openshiftdb.server.projects
import io.github.hoeggi.openshiftdb.server.route
import io.github.hoeggi.openshiftdb.server.secrets
import io.github.hoeggi.openshiftdb.server.server
import io.github.hoeggi.openshiftdb.server.services
import io.github.hoeggi.openshiftdb.server.version
import io.github.hoeggi.openshiftdb.server.webSocket
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post

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
