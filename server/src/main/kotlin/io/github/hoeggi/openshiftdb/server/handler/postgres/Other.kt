package io.github.hoeggi.openshiftdb.server.handler.postgres

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.ToolsVersionApi
import io.github.hoeggi.openshiftdb.postgres.Postgres
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal fun Tools(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
    val awaitAll = awaitAll(
        async { Postgres.psqlVersion() },
        async { Postgres.pqDumpVersion() }
    )
    val psql = awaitAll[0]
    val pgDump = awaitAll[1]
    call.respond(ApiResponse(ToolsVersionApi(psql.first, pgDump.first), psql.second + pgDump.second))
}