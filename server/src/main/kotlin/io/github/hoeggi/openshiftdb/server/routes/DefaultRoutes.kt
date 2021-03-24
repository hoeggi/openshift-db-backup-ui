package io.github.hoeggi.openshiftdb.server.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
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
