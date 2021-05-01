package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.getOrDefault
import io.github.hoeggi.openshiftdb.api.response.DatabaseEventApi
import io.github.hoeggi.openshiftdb.api.response.EventApi
import io.github.hoeggi.openshiftdb.api.response.EventResultApi
import io.github.hoeggi.openshiftdb.api.response.EventTypeApi
import io.github.hoeggi.openshiftdb.api.response.PortForwardEventApi
import io.github.hoeggi.openshiftdb.api.response.Trackable
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.viewmodel.models.PortForwardTarget
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Random

internal class EventsViewModelImpl(port: Int, errorViewer: ErrorViewer) :
    BaseViewModelImpl(port, errorViewer), EventsViewModel {

    private val _events = MutableStateFlow(listOf<Pair<PortForwardEvent, List<DatabaseEvent>>>())
    override val events = _events.asStateFlow()
    override fun events() = coroutineScope.launch {

        val dbe = async { eventsApi.databaseEvents() }
        val pfe = async { eventsApi.portForwardEvents() }
        awaitAll(dbe, pfe)
        val databaseEvents = dbe.getCompleted()
        val portForwardEvents = pfe.getCompleted()
        _events.value = portForwardEvents
            .getOrDefault(listOf())
            .map { forward ->
                val portForwardUiEvent = forward.toUiEvent()
                if (forward.port == forwardablePort) {
                    val map = databaseEvents
                        .getOrDefault(listOf())
                        .filter {
                            it.startTime.isAfter(forward.startTime) &&
                                it.startTime.isBefore(forward.endTime ?: LocalDateTime.now())
                        }.map {
                            it.toUiEvent(portForwardUiEvent.color)
                        }
                    portForwardUiEvent to map
                } else portForwardUiEvent to listOf()
            }.sortedBy {
                it.first.start
            }.reversed()
    }

    companion object {
        private const val forwardablePort = 5432
    }
}

internal class DatabaseEventTracker(
    var path: String,
    private val currentUsername: String,
    private val database: String,
    private val format: String,
) : EventTracker() {

    override fun event() = DatabaseEventApi(
        dbname = database,
        path = path,
        username = currentUsername,
        format = format,
        startTime = start!!,
        endTime = end!!,
        eventType = type!!,
        result = result!!,
    )
}

internal class PortForwardEventTracker(
    private val portForwardTarget: PortForwardTarget,
) : EventTracker() {
    private val random by lazy { Random() }
    fun stopped() {
        end = LocalDateTime.now()
        result = EventResultApi.Success
    }

    override fun event() = PortForwardEventApi(
        project = portForwardTarget.project,
        service = portForwardTarget.svc,
        port = portForwardTarget.port,
        color = randomColor(),
        startTime = start!!,
        endTime = end,
        eventType = type!!,
        result = result,
    )

    private fun randomColor(): String {
        val randNum = random.nextInt(0xffffff + 1)
        return String.format("ff%06x", randNum)
    }
}

internal abstract class EventTracker {
    private val logger = LoggerFactory.getLogger(EventTracker::class.java)
    protected var start: LocalDateTime? = null
        set(value) {
            if (field == null) field = value
            else logger.debug("start already set, ignoring new value")
        }
    protected var end: LocalDateTime? = null
        set(value) {
            if (field == null) field = value
            else logger.debug("end already set, ignoring new value")
        }
    protected var type: EventTypeApi? = null
        set(value) {
            if (field == null) field = value
            else logger.debug("type already set, ignoring new value")
        }
    protected var result: EventResultApi? = null
        set(value) {
            if (field == null) field = value
            else logger.debug("result already set, ignoring new value")
        }

    fun trackMessage(message: Trackable) {
        when (message) {
            is Trackable.Start -> {
                start = LocalDateTime.now()
                type = when (message.eventType) {
                    Trackable.Type.Restore -> EventTypeApi.Restore
                    Trackable.Type.Dump -> EventTypeApi.Dump
                    Trackable.Type.PortForward -> EventTypeApi.PortForward
                }
            }
            is Trackable.Error -> {
                end = LocalDateTime.now()
                result = EventResultApi.Error
            }
            is Trackable.Finish -> {
                end = LocalDateTime.now()
                result = EventResultApi.Success
            }
        }
    }

    abstract fun event(): EventApi
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
