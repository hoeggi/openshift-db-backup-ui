package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.ClusterApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

fun Cluster(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val listServer = OC.listServer()
        if (listServer.server.isEmpty()) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(
                ApiResponse(
                    listServer.server.map {
                        ClusterApi(
                            name = it.name,
                            server = it.cluster.server

                        )
                    },
                    result = listServer.result
                )
            )
        }
    }