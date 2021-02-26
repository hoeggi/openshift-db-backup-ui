package io.github.hoeggi.openshiftdb.server.routes

import io.github.hoeggi.openshiftdb.server.handler.postgres.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*

fun Route.postgres() {
    route("/postgres") {
        get {
            call.respond(HttpStatusCode.OK, "/postgres")
        }
        route("/version") {
            get("/tools", Tools())
            authenticate("postgres") {
                get("/database", DatabaseVersion())
            }
        }
        authenticate("postgres") {
            route("/databases") {
                get(Databases())
                get("/default", DefaultDatabase())
                webSocket("/dump", null, PostgresDump())
            }
        }
    }
}
