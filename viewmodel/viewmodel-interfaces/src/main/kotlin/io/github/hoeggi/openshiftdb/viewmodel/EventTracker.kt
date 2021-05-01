package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.response.DatabaseEventApi
import io.github.hoeggi.openshiftdb.api.response.EventResultApi
import io.github.hoeggi.openshiftdb.api.response.EventTypeApi
import io.github.hoeggi.openshiftdb.api.response.PortForwardEventApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface EventsViewModel : BaseViewModel {
    val events: StateFlow<List<Pair<PortForwardEvent, List<DatabaseEvent>>>>
    fun events(): Job
}

class Argb(val r: Int, val g: Int, val b: Int, val a: Int = "ff".toInt(16))
sealed class ColoredEvent(
    val color: Argb,
    val start: LocalDateTime,
    end: LocalDateTime?,
    val isSuccess: Boolean,
) {
    val startDate = start.format(DateTimeFormatter.ISO_DATE)
    val startTime = start.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val endDate = end?.format(DateTimeFormatter.ISO_DATE) ?: ""
    val endTime = end?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: ""

    val isFinished = end != null
}

private fun String.argb() = Argb(
    a = this.substring(0, 2).toInt(16),
    r = this.substring(2, 4).toInt(16),
    g = this.substring(4, 6).toInt(16),
    b = this.substring(6, 8).toInt(16)
)

private fun PortForwardEventApi.toUiEvent() = PortForwardEvent(
    project = project,
    service = service,
    port = port,
    color = color.argb(),
    start = startTime,
    end = endTime,
    success = when (result) {
        EventResultApi.Success -> true
        EventResultApi.Error -> false
        else -> false
    },
)

class PortForwardEvent(
    val project: String,
    val service: String,
    val port: Int,
    color: Argb,
    start: LocalDateTime,
    end: LocalDateTime?,
    success: Boolean,
) : ColoredEvent(
    color,
    start,
    end,
    success,
)

private fun DatabaseEventApi.toUiEvent(color: Argb) = DatabaseEvent(
    dbname = dbname,
    path = path,
    username = username,
    format = format,
    isDownload = when (eventType) {
        EventTypeApi.Dump -> true
        EventTypeApi.Restore -> false
        else -> throw UnsupportedOperationException("only restore and dump supported")
    },
    color = color,
    start = startTime,
    end = endTime,
    success = when (result) {
        EventResultApi.Success -> true
        EventResultApi.Error -> false
    },
)

class DatabaseEvent(
    val dbname: String,
    val path: String,
    val username: String,
    val format: String,
    val isDownload: Boolean,
    color: Argb,
    start: LocalDateTime,
    end: LocalDateTime,
    success: Boolean,
) : ColoredEvent(
    color,
    start,
    end,
    success,
)
