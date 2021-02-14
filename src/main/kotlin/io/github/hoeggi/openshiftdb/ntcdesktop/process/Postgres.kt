package io.github.hoeggi.openshiftdb.ntcdesktop.process

import io.github.hoeggi.openshiftdb.ntcdesktop.DUMP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import kotlin.jvm.Throws

class Postgres {

    sealed class PostgresResult {
        class Dump(private val process: Process, path: File) : PostgresResult(), Closeable {

            val stream = process.inputStream.source().buffer()
            val errorStream = process.errorStream.source().buffer()

            val outputPath = path.absolutePath
            val output = path.sink().buffer()

            val exitCode
                @Throws(IllegalThreadStateException::class)
                get() = process.exitValue()

            fun await() = process.waitFor()

            fun onFinish(onFinish: (Process) -> Unit) {
                process.onExit().thenAcceptAsync(onFinish)
            }

            override fun close() {
                RuntimeException("closed").printStackTrace()
                try {
                    stream.close()
                    errorStream.close()
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
            class Error(val exitCode: Int, val  ex: Exception?) : Download()
        }
    }

    private fun ProcessBuilder.withPassword(password: String) = also {
        println("PGPASSWORD='$password'")
        it.environment()["PGPASSWORD"] = password
    }

    private fun Process.await() = also {
        it.onExit().thenAcceptAsync {
            println("postges onExit: ${it.exitValue()} - ${Thread.currentThread().name}")
        }
        it.waitFor()
    }

    suspend fun dumpDatabase(databse: String, path: String, password: String) = withContext(Dispatchers.DUMP) {
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

    suspend fun listPretty(password: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(listPretty)
            .withPassword(password)
            .start()
            .await()
            .text()
    }

    suspend fun listLines(password: String): List<String> {
        return list(password).split("\n").filter { it.isNotEmpty() }
    }

    suspend fun list(password: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(list)
            .withPassword(password)
            .start()
            .await()
            .text()
    }

    suspend fun psqlVersion() = withContext(Dispatchers.IO) {
        ProcessBuilder(
            "psql", "-V"
        ).start().also {
            it.waitFor()
        }.text()
    }

    suspend fun pq_dumpVersion() = withContext(Dispatchers.IO) {
        ProcessBuilder(
            "pg_dump", "--version"
        ).start().also {
            it.waitFor()
        }.text()
    }

    suspend fun postgresVersion(password: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(version)
            .withPassword(password)
            .start()
            .await()
            .text()
    }

    companion object {
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

        fun dump(databse: String) = listOf(
            "pg_dump",
            "--user=postgres",
            "--host=localhost",
            "--port=5432",
            "--clean", "--create", "--format=p",
            "$databse",
        )
    }
}