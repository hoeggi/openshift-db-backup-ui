package io.github.hoeggi.openshiftdb.process

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.IOException

class DatabaseDownloader {

    suspend fun download(
        dump: Postgres.PostgresResult.Dump,
        onNewLine: (String) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (Int, Exception?) -> Unit,
    ) = withContext(DISPATCHER) {
        try {

            val input = async(DISPATCHER) {
                val output = dump.output
                try {
                    while (true) {
                        val line = dump.buffer.readUtf8Line() ?: break
                        output.writeUtf8(line)
                        output.writeUtf8("\n")

                        onNewLine(line)
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    throw ex
                }
            }
            val error = async(DISPATCHER) {
                try {
                    while (true) {
                        val line = dump.bufferError.readUtf8Line() ?: break
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