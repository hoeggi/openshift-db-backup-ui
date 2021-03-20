package io.github.hoeggi.openshiftdb.eventlog

enum class EventType {
    Dump, Restore, PortForward
}

enum class EventResult {
    Error, Success
}