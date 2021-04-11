package io.github.hoeggi.openshiftdb.api.response

import kotlinx.serialization.Serializable

@Serializable
data class LogEvent(
    val timestamp: Long,
    val message: String,
    val loggerName: String,
    val logLevel: String,
    val thread_name: String?,
    val eventId: Long,
    val caller: String,
    val properties: List<LogProperty>,
    val exception: List<LogException>
)

@Serializable
data class LogProperty(
    val key: String,
    val value: String?
)

@Serializable
data class LogException(
    val index: Long,
    val line: String
)
