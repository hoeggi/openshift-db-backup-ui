package io.github.hoeggi.openshiftdb.postgres

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.IOException

class DatabaseRestore(
    private val restore: Postgres.PostgresResult.Restore,
    private val onNewLine: suspend (String) -> Unit,
    private val onSuccess: suspend (String, String) -> Unit,
    private val onError: suspend (Int, Exception?) -> Unit,
) {

    private val logger = LoggerFactory.getLogger(DatabaseDownloaderPlain::class.java)

    suspend fun restore() = withContext(Dispatchers.IO) {
        try {
            restore.start()
            val input = async(Dispatchers.IO) {
                try {
                    while (true) {
                        val line = restore.buffer.readUtf8Line() ?: break
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
                        val line = restore.bufferError.readUtf8Line() ?: break
                        onNewLine(line)
                    }
                } catch (ex: IOException) {
                    logger.error("error downloading database", ex)
                    throw ex
                }
            }

            awaitAll(input, error)
            delay(500)
            if (restore.exitCode == 0) onSuccess(restore.backup, restore.database)
            else onError(restore.exitCode, null)
        } catch (ex: Exception) {
            logger.warn("unable to restore database", ex)
            onError(restore.exitCode, ex)
        } finally {
            restore.close()
        }
    }
}
