package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

internal suspend inline fun <reified T> PipelineContext<Unit, ApplicationCall>.authorized(
    result: T,
    code: Int
) {
    if (code == 1) {
        call.respond(HttpStatusCode.Unauthorized)
    } else {
        call.respond(ApiResponse(result, code))
    }
}