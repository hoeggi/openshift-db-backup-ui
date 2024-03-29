package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.VersionApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

internal fun Version(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val version = OC.version()
        version.version?.run {
            call.respond(
                ApiResponse(
                    VersionApi(
                        oc = releaseClientVersion,
                        openshift = openshiftVersion,
                        kubernets = serverVersion.gitVersion
                    ),
                    result = version.result
                )
            )
        } ?: call.respond(HttpStatusCode.NotFound)
    }
