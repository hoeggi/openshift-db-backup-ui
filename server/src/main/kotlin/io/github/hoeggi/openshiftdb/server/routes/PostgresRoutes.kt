package io.github.hoeggi.openshiftdb.server.routes

import io.github.hoeggi.openshiftdb.server.Path
import io.github.hoeggi.openshiftdb.server.command
import io.github.hoeggi.openshiftdb.server.database
import io.github.hoeggi.openshiftdb.server.databases
import io.github.hoeggi.openshiftdb.server.default
import io.github.hoeggi.openshiftdb.server.dump
import io.github.hoeggi.openshiftdb.server.get
import io.github.hoeggi.openshiftdb.server.handler.postgres.DatabaseVersion
import io.github.hoeggi.openshiftdb.server.handler.postgres.Databases
import io.github.hoeggi.openshiftdb.server.handler.postgres.DefaultDatabase
import io.github.hoeggi.openshiftdb.server.handler.postgres.PostgresDump
import io.github.hoeggi.openshiftdb.server.handler.postgres.RestoreCommand
import io.github.hoeggi.openshiftdb.server.handler.postgres.RestoreDatabase
import io.github.hoeggi.openshiftdb.server.handler.postgres.RestoreInfo
import io.github.hoeggi.openshiftdb.server.handler.postgres.Tools
import io.github.hoeggi.openshiftdb.server.info
import io.github.hoeggi.openshiftdb.server.postgres
import io.github.hoeggi.openshiftdb.server.restore
import io.github.hoeggi.openshiftdb.server.route
import io.github.hoeggi.openshiftdb.server.tools
import io.github.hoeggi.openshiftdb.server.version
import io.github.hoeggi.openshiftdb.server.webSocket
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.websocket.webSocket

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
