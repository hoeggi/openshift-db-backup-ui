package io.github.hoeggi.openshiftdb.postgres

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.IOException

class DatabaseDownloader(
    private val dump: Postgres.PostgresResult.Dump,
    private val onNewLine: suspend (String) -> Unit,
    private val onSuccess: suspend (String) -> Unit,
    private val onError: suspend (Int, Exception?) -> Unit,
) {

    val logger = LoggerFactory.getLogger(DatabaseDownloader::class.java)

    suspend fun download() = withContext(Dispatchers.IO) {
        try {

            val input = async(Dispatchers.IO) {
                val output = dump.output
                try {
                    while (true) {
                        val line = dump.buffer.readUtf8Line() ?: break
                        output.writeUtf8(line)
                        output.writeUtf8("\n")
                        onNewLine(line)
                    }
                } catch (ex: IOException) {
                    logger.error("error downloading database", ex)
                    throw ex
                }
            }
            val error = async(Dispatchers.IO) {
                try {
                    while (true) {
                        val line = dump.bufferError.readUtf8Line() ?: break
                        onNewLine(line)
                    }
                } catch (ex: IOException) {
                    logger.error("error downloading database", ex)
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