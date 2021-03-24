package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.ServicesApi
import io.github.hoeggi.openshiftdb.api.response.ServicesPortApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

internal fun Services(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val services = OC.services()
        if (services.services.isEmpty()) {
            if (services.result == 1) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } else {
            call.respond(
                ApiResponse(
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
                    result = services.result
                )
            )
        }
    }
