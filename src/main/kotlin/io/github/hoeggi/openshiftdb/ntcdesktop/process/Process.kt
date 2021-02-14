package io.github.hoeggi.openshiftdb.ntcdesktop.process

sealed class ProcessResult(val code: Int = -1) {
    object Unset : ProcessResult()
    object Ok : ProcessResult(0)
    class Error(result: Int) : ProcessResult(result)
}

interface Command {
    val commands: List<String>
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

