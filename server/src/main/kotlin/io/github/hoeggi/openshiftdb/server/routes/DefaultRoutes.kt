package io.github.hoeggi.openshiftdb.server.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry

internal fun Route.default(appMicrometerRegistry: PrometheusMeterRegistry) {
    get("/metrics") {
        call.respond(appMicrometerRegistry.scrape())
    }
    get("/echo/{message?}") {
        call.respondText("Echo, ${call.parameters["message"]}")
    }
    get("/status") {
        call.respond(HttpStatusCode.OK)
    }
}