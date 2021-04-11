package io.github.hoeggi.openshiftdb.eventlog

enum class EventType {
    Dump, Restore, PortForward
}

enum class EventResult {
    Error, Success
}

// data class Logs(
//    val event: LogEvent,
//    val exceptions: List<LogsExceptions> = listOf(),
// )
//
// data class LogsExceptions(
//    val exception: LogException,
//    val traces: List<LogsTraces> = listOf(),
// )
//
// inline class LogsTraces(
//    val trace: LogTrace,
// )
