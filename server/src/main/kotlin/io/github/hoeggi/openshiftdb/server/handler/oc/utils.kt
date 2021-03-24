package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

internal suspend inline fun <reified T> PipelineContext<Unit, ApplicationCall>.authorized(
    result: T,
    code: Int,
) {
    if (code == 1) {
        call.respond(HttpStatusCode.Unauthorized)
    } else {
        call.respond(ApiResponse(result, code))
    }
}
