package io.github.hoeggi.openshiftdb.process

import io.ktor.application.*
import io.ktor.auth.*
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.Executors

internal val DISPATCHER = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
).asCoroutineDispatcher()

class Postgres {

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
            println("postges onExit: ${it.exitValue()} - ${Thread.currentThread().name}")
//            if (it.exitValue() != 0) {
//                println(it.errorStream.source().buffer().readUtf8())
//            }
        }
        it.waitFor()
    }

    suspend fun dumpDatabase(databse: String, path: String, password: String) = withContext(DISPATCHER) {
        PostgresResult.Dump(
            ProcessBuilder(dump(databse))
                .withPassword(password)
                .start()
                .also {
                    it.onExit().thenAcceptAsync {
                        println("dumpDatabase onExit: ${it.exitValue()} - ${Thread.currentThread().name}")
                    }
                },
            File(path, "$databse-${LocalDateTime.now().toString().replace(" ", "-")}")
        )
    }

    suspend fun listPretty(password: String) = withContext(DISPATCHER) {
        ProcessBuilder(listPretty)
            .withPassword(password)
            .start()
            .await()
            .text()
    }

    suspend fun listLines(password: String): List<String> {
        return list(password).split("\n").filter { it.isNotEmpty() }
    }

    suspend fun defaultDB(password: String) = withContext(DISPATCHER) {
        ProcessBuilder(defaultDb)
            .withPassword(password)
            .start()
            .await()
            .text()
    }

    suspend fun list(password: String) = withContext(DISPATCHER) {
        ProcessBuilder(list)
            .withPassword(password)
            .start()
            .await()
            .text()
    }

    suspend fun psqlVersion() = withContext(DISPATCHER) {
        ProcessBuilder(
            "psql", "-V"
        ).start().also {
            it.waitFor()
        }.text()
    }

    suspend fun pqDumpVersion() = withContext(DISPATCHER) {
        ProcessBuilder(
            "pg_dump", "--version"
        ).start().also {
            it.waitFor()
        }.text()
    }

    suspend fun postgresVersion(password: String) = withContext(DISPATCHER) {
        ProcessBuilder(version)
            .withPassword(password)
            .start()
            .await()
            .text()
    }

    companion object {
        data class PostgresPrincibal(val username: String, val password: String) : Principal

        fun validateCredentials(): suspend ApplicationCall.(UserPasswordCredential) -> Principal? =
            { credentials ->
                val result = ProcessBuilder(checkConnection(credentials.name))
                    .also {
                        it.environment()["PGPASSWORD"] = credentials.password
                    }.start().waitFor()
                if (result == 0) {
                    PostgresPrincibal(credentials.name, credentials.password)
                } else {
                    null
                }
            }

        fun checkConnection(username: String) = listOf(
            "psql",
            "-h", "localhost", "-p", "5432", "-U", username,
            "-c", "\\q"
        )

        val list = listOf(
            "psql",
            "-h", "localhost", "-p", "5432", "-U", "postgres",
            "-q", "-A", "-t",
            "-c", "SELECT datname FROM pg_database"
        )

        val listPretty = listOf(
            "psql",
            "-h", "localhost",
            "-p", "5432",
            "-U", "postgres",
            "-c", "\\l"
        )

        val version = listOf(
            "psql",
            "-h", "localhost", "-p", "5432", "-U", "postgres",
            "-q", "-A", "-t",
            "-c", "select version();"
        )

        val defaultDb = listOf(
            "psql",
            "-h", "localhost", "-p", "5432", "-U", "postgres",
            "-q", "-A", "-t",
            "-c", "select current_database();"
        )

        fun dump(databse: String) = listOf(
            "pg_dump",
            "--user=postgres",
            "--host=localhost",
            "--port=5432",
            "--clean", "--create", "--format=p",
            databse,
        )
    }
}