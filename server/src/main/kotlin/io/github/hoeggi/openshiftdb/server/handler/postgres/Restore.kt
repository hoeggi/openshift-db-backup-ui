package io.github.hoeggi.openshiftdb.server.handler.postgres

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import io.github.hoeggi.openshiftdb.api.response.RestoreCommandApi
import io.github.hoeggi.openshiftdb.api.response.RestoreInfoApi
import io.github.hoeggi.openshiftdb.postgres.Postgres
import io.github.hoeggi.openshiftdb.postgres.PostgresPrincibal
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal fun RestoreCommand(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
    val path = call.request.queryParameters["path"]
    val principal = call.principal<PostgresPrincibal>()
    val username = principal?.username
    val password = principal?.password

    if (principal == null || username == null || password == null) {
        call.respond(HttpStatusCode.Unauthorized)
    } else if (path == null) {
        call.respond(HttpStatusCode.BadRequest, "missing path")
    } else {
        val dbs = async {
            Postgres.listLines(username, password).first
        }
        val restore = async {
            Postgres.restoreInfo(path).first.split("\n")
                .firstOrNull { it.contains("dbname:") }
                ?.run {
                    substring(indexOf(":") + 1).trim()
                }
        }
        awaitAll(dbs, restore)

        val databases = dbs.getCompleted()
        val restoreDB = restore.getCompleted()
        if (restoreDB.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "unsupported format")
        } else {
            val isNew = !databases.contains(restoreDB)
            val command =
                if (isNew) Postgres.restoreCommand(username, password, path)
                else Postgres.restoreCommand(username, password, path, restoreDB)

            call.respond(ApiResponse(RestoreCommandApi(command.command(), !isNew, Postgres.DEFAULT_DB, restoreDB), 0))
        }
    }
}

internal fun RestoreInfo(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = {
    val path = call.request.queryParameters["path"]
    if (path.isNullOrBlank()) {
        call.respond(HttpStatusCode.BadRequest, "missing path")
    } else {
        val restoreInfo = Postgres.restoreInfo(path)
        if (restoreInfo.second != 0) {
            call.respond(HttpStatusCode.BadRequest, restoreInfo.first)
        } else {
            val infoLines = restoreInfo.first.split("\n").asSequence()
            val info = async {
                infoLines
                    .takeWhile { it.startsWith(";") }
                    .filter { it != ";" }
                    .map { it.replace(";", "").trim() }
                    .toMutableList()
                    .apply {
                        removeLast()
                    }
            }
            val extensions = async {
                "extensions: ${
                infoLines
                    .filter { it.contains("COMMENT - EXTENSION") }
                    .map { it.trim().substring(it.trim().lastIndexOf(" ")) }
                    .toList()
                }"
            }
            val users = async {
                "hdb_role: ${
                infoLines
                    .filter { it.contains("hdb_role") }
                    .map { it.trim().substring(it.trim().lastIndexOf(" ")) }
                    .toList()
                }"
            }
            awaitAll(info, extensions, users)
            val filteredInfo = info.getCompleted().apply {
                add(extensions.getCompleted())
                add(users.getCompleted())
            }.toList()
            call.respond(ApiResponse(RestoreInfoApi(filteredInfo), restoreInfo.second))
        }
    }
}
