package io.github.hoeggi.openshiftdb.commons

import okio.buffer
import okio.source

interface Command {
    val commands: List<String>
}

sealed class ProcessResult(val code: Int = -1) {
    object Unset : ProcessResult()
    object Ok : ProcessResult(0)
    class Error(result: Int) : ProcessResult(result)
}

fun Process.buffer() = inputStream.source().buffer()
fun Process.readStdout() = inputStream.source().buffer().readUtf8()

fun Process.bufferError() = errorStream.source().buffer()
fun Process.readError() = errorStream.source().buffer().readUtf8()

fun Process.messageAndResult() = if (exitValue() == 0) {
    inputStream.bufferedReader().readText() to exitValue()
} else {
    errorStream.bufferedReader().readText()to exitValue()
}

fun Process.text() = if (exitValue() == 0) {
    inputStream.bufferedReader().readText()
} else {
    errorStream.bufferedReader().readText()
}

fun Process.lines() = if (exitValue() == 0) {
    inputStream.bufferedReader().readLines()
} else {
    listOf(errorStream.bufferedReader().readText())
}

fun Process.result() = if (exitValue() == 0) {
    ProcessResult.Ok
} else {
    ProcessResult.Error(exitValue())
}

fun process(command: Command): Process = ProcessBuilder(command.commands).start().also {
    it.waitFor()
}
