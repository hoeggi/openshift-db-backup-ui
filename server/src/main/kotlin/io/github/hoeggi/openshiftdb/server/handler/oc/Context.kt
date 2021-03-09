package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.*
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

fun SwitchContext(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
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

fun Context(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
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