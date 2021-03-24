package io.github.hoeggi.openshiftdb.server.handler.postgres

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.DatabaseEventApi
import io.github.hoeggi.openshiftdb.api.response.EventApi
import io.github.hoeggi.openshiftdb.api.response.EventResultApi
import io.github.hoeggi.openshiftdb.api.response.EventTypeApi
import io.github.hoeggi.openshiftdb.api.response.PortForwardEventApi
import io.github.hoeggi.openshiftdb.api.response.ToolsVersionApi
import io.github.hoeggi.openshiftdb.eventlog.DatabaseEvent
import io.github.hoeggi.openshiftdb.eventlog.DatabaseLogProvider
import io.github.hoeggi.openshiftdb.eventlog.EventResult
import io.github.hoeggi.openshiftdb.eventlog.EventType
import io.github.hoeggi.openshiftdb.eventlog.PortForwardEvent
import io.github.hoeggi.openshiftdb.postgres.Postgres
import io.github.hoeggi.openshiftdb.server.Path
import io.github.hoeggi.openshiftdb.server.database
import io.github.hoeggi.openshiftdb.server.portForward
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
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

internal object TransactionLogger {
    val databaseLog = DatabaseLogProvider.DatabaseLog

    val transactions: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
        val path = call.request.path()
        val events = when {
            path.endsWith(Path.database().path) ->
                databaseLog.listAllDatabaseEvents()
                    .map { it.toDatabaseEventApi() }
            path.endsWith(Path.portForward().path) ->
                databaseLog.listAllPortForwardEvents()
                    .map { it.toPortForwardEventApi() }
            else -> null
        }
        if (events == null) call.respond(HttpStatusCode.BadRequest, "invalid path")
        else call.respond(ApiResponse(events, -1))
    }

    val newTransaction: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
        val log = call.receiveOrNull<EventApi>()
        if (log == null) {
            call.respond(HttpStatusCode.BadRequest, "missing request data")
        } else {
            val event = when (log) {
                is DatabaseEventApi -> databaseLog.insert(log.toDatabaseEvent())
                is PortForwardEventApi -> databaseLog.insert(log.toPortForwardEvent())
                else -> -1
            }
            call.respond(ApiResponse(event, -1))
        }
    }

    private fun DatabaseEvent.toDatabaseEventApi() = DatabaseEventApi(
        dbname = dbname,
        path = path,
        username = username,
        format = format,
        startTime = startTime,
        endTime = endTime,
        eventType = when (type) {
            EventType.Dump -> EventTypeApi.Dump
            EventType.Restore -> EventTypeApi.Restore
            else -> throw UnsupportedOperationException("only Dump and Restore are supported")
        },
        result = when (result) {
            EventResult.Success -> EventResultApi.Success
            EventResult.Error -> EventResultApi.Error
        },
    )

    private fun DatabaseEventApi.toDatabaseEvent() = DatabaseEvent(
        id = -1,
        dbname = dbname,
        path = path,
        username = username,
        format = format,
        startTime = startTime,
        endTime = endTime,
        type = when (eventType) {
            is EventTypeApi.Dump -> EventType.Dump
            is EventTypeApi.Restore -> EventType.Restore
            else -> throw UnsupportedOperationException("only Dump and Restore are supported")
        },
        result = when (result) {
            is EventResultApi.Success -> EventResult.Success
            is EventResultApi.Error -> EventResult.Error
        },
    )

    private fun PortForwardEvent.toPortForwardEventApi() = PortForwardEventApi(
        project = project,
        service = service,
        port = port,
        color = color,
        startTime = startTime,
        endTime = endTime,
        eventType = when (type) {
            EventType.PortForward -> EventTypeApi.PortForward
            else -> throw UnsupportedOperationException("only PortForward is supported")
        },
        result = when (result) {
            EventResult.Success -> EventResultApi.Success
            EventResult.Error -> EventResultApi.Error
            else -> null
        },
    )

    private fun PortForwardEventApi.toPortForwardEvent() = PortForwardEvent(
        id = -1,
        project = project,
        service = service,
        port = port,
        color = color,
        startTime = startTime,
        endTime = endTime,
        type = when (eventType) {
            is EventTypeApi.PortForward -> EventType.PortForward
            else -> throw UnsupportedOperationException("only PortForward is supported")
        },
        result = when (result) {
            is EventResultApi.Success -> EventResult.Success
            is EventResultApi.Error -> EventResult.Error
            else -> null
        },
    )
}
