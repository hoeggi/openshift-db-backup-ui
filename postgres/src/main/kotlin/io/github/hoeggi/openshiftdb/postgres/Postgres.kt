package io.github.hoeggi.openshiftdb.postgres

import io.github.hoeggi.openshiftdb.commons.buffer
import io.github.hoeggi.openshiftdb.commons.bufferError
import io.github.hoeggi.openshiftdb.commons.messageAndResult
import io.github.hoeggi.openshiftdb.commons.text
import io.ktor.application.*
import io.ktor.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.Executors

object Postgres {
    val logger = LoggerFactory.getLogger(Postgres::class.java)

    sealed class PostgresResult {
        class Dump(private val process: Process, path: File) : PostgresResult(), Closeable {
            val buffer = process.buffer()
            val bufferError = process.bufferError()

            val outputPath: String = path.absolutePath
            val output = path.sink().buffer()

            val exitCode
                @Throws(IllegalThreadStateException::class)
                get() = process.exitValue()

            override fun close() {
                try {
                    process.inputStream.close()
                    process.errorStream.close()
                    output.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }

        sealed class Download {
            object Unspecified : Download()
            object Started : Download()
            class InProgres(val lines: List<String>) : Download()
            class Success(val path: String) : Download()
            class Error(val exitCode: Int, val ex: Exception?) : Download()
        }
    }

    private fun ProcessBuilder.withPassword(password: String) = also {
        it.environment()["PGPASSWORD"] = password
    }

    private fun Process.await() = also {
        it.onExit().thenAcceptAsync {
            logger.debug("postges onExit: ${it.exitValue()} - ${Thread.currentThread().name}")
        }
        it.waitFor()
    }

    suspend fun dumpDatabase(database: String, path: String, username: String, password: String) =
        withContext(Dispatchers.IO) {
            val commands = Commands.PgDump.Dump(username, database).commands
            PostgresResult.Dump(
                ProcessBuilder(commands)
                    .withPassword(password)
                    .start()
                    .also {
                        it.onExit().thenAcceptAsync {
                            logger.debug("dumpDatabase onExit: ${it.exitValue()} - ${Thread.currentThread().name}")
                        }
                    },
                File(path, "$database-${LocalDateTime.now().toString().replace(" ", "-")}")
            )
        }

    suspend fun listPretty(username: String, password: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(Commands.Psql.WithUser.ListPretty(username).commands)
            .withPassword(password)
            .start()
            .await()
            .messageAndResult()
    }

    suspend fun listLines(username: String, password: String): Pair<List<String>, Int> {
        val list = list(username, password)
        return list.first.split("\n").filter { it.isNotEmpty() } to list.second
    }

    suspend fun defaultDB(username: String, password: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(Commands.Psql.WithUser.DefaultDb(username).commands)
            .withPassword(password)
            .start()
            .await()
            .messageAndResult()
    }

    suspend fun list(username: String, password: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(Commands.Psql.WithUser.List(username).commands)
            .withPassword(password)
            .start()
            .await()
            .messageAndResult()
    }

    suspend fun psqlVersion() = withContext(Dispatchers.IO) {
        ProcessBuilder(Commands.Psql.Version.commands).start().also {
            it.waitFor()
        }.messageAndResult()
    }

    suspend fun pqDumpVersion() = withContext(Dispatchers.IO) {
        ProcessBuilder(Commands.PgDump.Version.commands).start().also {
            it.waitFor()
        }.messageAndResult()
    }

    suspend fun postgresVersion(username: String, password: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(Commands.Psql.WithUser.DatabaseVersion(username).commands)
            .withPassword(password)
            .start()
            .await()
            .messageAndResult()
    }
}