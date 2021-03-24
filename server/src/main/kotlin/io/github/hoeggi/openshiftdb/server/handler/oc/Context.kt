package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.ContextApi
import io.github.hoeggi.openshiftdb.api.response.ContextDetailApi
import io.github.hoeggi.openshiftdb.api.response.ContextsApi
import io.github.hoeggi.openshiftdb.api.response.SwitchContextApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal fun SwitchContext(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val context = call.receiveOrNull<SwitchContextApi>()
        if (context == null) {
            call.respond(HttpStatusCode.BadRequest, "missing context")
        } else {
            val listContext = OC.switchContext(context.context)
            call.respond(
                ApiResponse(
                    SwitchContextApi(listContext.newContext),
                    result = listContext.result
                )
            )
        }
    }

internal fun Context(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val allJob = async { OC.listContext() }
        val currentJob = async { OC.currentContext() }
        awaitAll(allJob, currentJob)
        val listContext = allJob.getCompleted()

        if (listContext.contexts.isEmpty()) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val response = listContext.contexts.groupBy(
                keySelector = {
                    it.context.cluster
                },
                valueTransform = {
                    ContextDetailApi(it.name, it.context.user, it.context.namespace)
                }
            ).map {
                ContextsApi(it.key, it.value)
            }
            call.respond(ApiResponse(ContextApi(currentJob.getCompleted().context, response), listContext.result))
        }
    }
