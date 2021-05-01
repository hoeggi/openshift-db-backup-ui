package io.github.hoeggi.openshiftdb.viewmodel

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

data class LogLine(
    val logLevel: String,
    val line: String,
)

interface SyslogViewModel : BaseViewModel {
    val syslog: StateFlow<List<LogLine>>
    fun load(): Job
}
