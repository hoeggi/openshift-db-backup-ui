package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.response.LogEvent
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class LogLine(
    val logLevel: String,
    val line: String,
)

class SyslogViewModel internal constructor(port: Int, errorViewer: ErrorViewer) :
    BaseViewModel(port, errorViewer) {

    init {
        load()
    }

    private val _log: MutableStateFlow<List<LogLine>> =
        MutableStateFlow(listOf())

    val syslog = _log.asStateFlow()
    fun load() = coroutineScope.launch {
        loggingApi.log()
            .map {
                it.flatMap { it.toLogLines() }
            }.collect {
                _log.value = it
            }
    }

    private fun LogEvent.toLogLines() = mutableListOf<LogLine>().apply {
        add(
            LogLine(
                logLevel = logLevel,
                line = "$logLevel $time [$caller] $message"
            )
        )
        exception.forEach {
            add(
                LogLine(
                    logLevel = logLevel,
                    line = it.line.replace("\t", "  ")
                )
            )
        }
    }

    private val LogEvent.time: String
        get() = LocalTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).format(formatter)

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    }
}
