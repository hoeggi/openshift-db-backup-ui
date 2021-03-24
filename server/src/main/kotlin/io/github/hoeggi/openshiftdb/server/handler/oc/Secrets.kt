package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.SecretsApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

internal fun Password(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val username = call.request.queryParameters["username"]
        if (username == null) {
            call.respond(HttpStatusCode.BadRequest, "missing username")
        } else {
            val secret = OC.password(username)
            if (secret.password.isNullOrEmpty()) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(ApiResponse(secret.password, secret.result))
            }
        }
    }

@Suppress("UNCHECKED_CAST")
internal fun Secrets(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val secrets = OC.secrets()
        if (secrets.secrets.isNullOrEmpty()) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(
                ApiResponse(
                    secrets.secrets.map {
                        SecretsApi(it.metadata.name, it.data.filter { it.value != null } as Map<String, String>)
                    },
                    secrets.result
                )
            )
        }
    }
