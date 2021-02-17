package io.github.hoeggi.openshiftdb.process

import io.github.hoeggi.openshiftdb.DUMP
import kotlinx.coroutines.*
import java.io.IOException

class DatabaseDownloader {

    suspend fun download(
        dump: Postgres.PostgresResult.Dump,
        onNewLine: (String) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (Int, Exception?) -> Unit,
    ) = withContext(Dispatchers.DUMP) {
        try {

            val input = async(Dispatchers.DUMP) {
                val output = dump.output
                try {
                    while (true) {
                        val line = dump.stream.readUtf8Line() ?: break
                        output.writeUtf8(line)
                        output.writeUtf8("\n")

                        onNewLine(line)
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    throw ex
                }
            }
            val error = async(Dispatchers.DUMP) {
                try {
                    while (true) {
                        val line = dump.errorStream.readUtf8Line() ?: break
                        onNewLine(line)
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    throw ex
                }
            }

            input.await()
            error.await()
            onSuccess(dump.outputPath)
        } catch (ex: Exception) {
            ex.printStackTrace()
            onError(dump.exitCode, ex)
        } finally {
            dump.close()
        }
    }

}