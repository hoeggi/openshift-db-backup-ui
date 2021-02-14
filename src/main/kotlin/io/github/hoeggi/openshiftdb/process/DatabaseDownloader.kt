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
                        println("---reading line---")
                        val line = dump.stream.readUtf8Line() ?: break
                        println("---read line: $line---")
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
                        println("---reading errorline---")
                        val line = dump.errorStream.readUtf8Line() ?: break
                        println("---read errorline: $line---")
                        onNewLine(line)
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    throw ex
                }
            }

            input.await()
            error.await()
//            runBlocking {
//                dump.await()
//            }
            onSuccess(dump.outputPath)
        } catch (ex: Exception) {
            println("----------------------------------------------")
            ex.printStackTrace()
            println("----------------------------------------------")
            onError(dump.exitCode, ex)
        } finally {
            dump.close()
        }
    }

}