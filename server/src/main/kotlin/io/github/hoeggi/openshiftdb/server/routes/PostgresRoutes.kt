package io.github.hoeggi.openshiftdb.server.routes

import io.github.hoeggi.openshiftdb.server.*
import io.github.hoeggi.openshiftdb.server.handler.postgres.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*

internal fun Route.postgres() {
    route(Path.postgres()) {
        get {
            call.respond(HttpStatusCode.OK, "/postgres")
        }
        route(Path.version()) {
            get(Path.tools(), Tools())
            authenticate("postgres") {
                get(Path.database(), DatabaseVersion())
            }
        }
        authenticate("postgres") {
            route(Path.databases()) {
                get(Databases())
                get(Path.default(), DefaultDatabase())
                webSocket(Path.dump(), PostgresDump())
            }
        }
        route(Path.restore()) {
            get(Path.info(), RestoreInfo())
            authenticate("postgres") {
                get(Path.command(), RestoreCommand())
                webSocket(handler = RestoreDatabase())
            }
        }
    }
}
