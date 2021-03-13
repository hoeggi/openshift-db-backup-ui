package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.VersionApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

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