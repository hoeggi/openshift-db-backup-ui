package io.github.hoeggi.openshiftdb.postgres

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.IOException

class DatabaseDownloaderPlain(
    private val dump: Postgres.PostgresResult.DumpPlain,
    private val onNewLine: suspend (String) -> Unit,
    private val onSuccess: suspend (String) -> Unit,
    private val onError: suspend (Int, Exception?) -> Unit,
) {

    val logger = LoggerFactory.getLogger(DatabaseDownloaderPlain::class.java)

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
            logger.warn("unable to dump database", ex)
            onError(dump.exitCode, ex)
        } finally {
            dump.close()
        }
    }
}

class DatabaseDownloaderCustom(
    private val dump: Postgres.PostgresResult.DumpCustom,
    private val onSuccess: suspend () -> Unit,
    private val onError: suspend (Int, String?, Exception?) -> Unit,
) {

    val logger = LoggerFactory.getLogger(DatabaseDownloaderPlain::class.java)

    suspend fun download() = withContext(Dispatchers.IO) {
        try {
            val exitCode = async {
                logger.debug("awaiting exit")
                dump.await()
            }.await()
            logger.debug("got exit code $exitCode")
            if (exitCode != 0) {
                logger.debug(dump.bufferError.readUtf8Line())
            }
            when (exitCode) {
                0 -> onSuccess()
                else -> onError(exitCode, dump.bufferError.readUtf8Line(), null)
            }
        } catch (ex: Exception) {
            logger.error("unable to dump database", ex)
            onError(dump.exitCode, null, ex)
        }
    }
}