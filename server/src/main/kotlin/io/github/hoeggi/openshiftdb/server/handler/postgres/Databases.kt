package io.github.hoeggi.openshiftdb.server.handler.postgres

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.DatabaseVersionApi
import io.github.hoeggi.openshiftdb.api.response.DatabasesApi
import io.github.hoeggi.openshiftdb.api.response.DefaultDatabaseApi
import io.github.hoeggi.openshiftdb.postgres.Postgres
import io.github.hoeggi.openshiftdb.postgres.PostgresPrincibal
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

internal fun DefaultDatabase(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
    val principal = call.principal<PostgresPrincibal>()
    if (principal == null) {
        call.respond(HttpStatusCode.Unauthorized)
    } else {
        val db = Postgres.defaultDB(principal.username, principal.password)
        call.respond(ApiResponse(DefaultDatabaseApi(db.first), db.second))
    }
}

internal fun Databases(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
    val format = call.request.queryParameters["format"]
    val principal = call.principal<PostgresPrincibal>()
    if (principal == null) {
        call.respond(HttpStatusCode.Unauthorized)
    } else {
        val apiResponse = when (format) {
            "list" -> {
                val db = Postgres.listLines(principal.username, principal.password)
                ApiResponse(DatabasesApi.List(db.first.toTypedArray()), db.second)
            }
            "table" -> {
                val db = Postgres.listPretty(principal.username, principal.password)
                ApiResponse(DatabasesApi.Tabel(db.first), db.second)
            }
            "text" -> {
                val db = Postgres.list(principal.username, principal.password)
                ApiResponse(DatabasesApi.Text(db.first), db.second)
            }
            else -> {
                val db = Postgres.listLines(principal.username, principal.password)
                ApiResponse(DatabasesApi.List(db.first.toTypedArray()), db.second)
            }
        }
        call.respond(apiResponse)
    }
}

internal fun DatabaseVersion(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
    val principal = call.principal<PostgresPrincibal>()
    if (principal == null) {
        call.respond(HttpStatusCode.Unauthorized)
    } else {
        val postgresVersion = Postgres.postgresVersion(principal.username, principal.password)
        call.respond(ApiResponse(DatabaseVersionApi(postgresVersion.first), postgresVersion.second))
    }
}