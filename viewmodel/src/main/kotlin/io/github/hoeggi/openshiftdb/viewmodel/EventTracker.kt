package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.response.*
import io.github.hoeggi.openshiftdb.viewmodel.models.PortForwardTarget
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

internal class DatabaseEventTracker(
    var path: String,
    private val currentUsername: String,
    private val database: String,
    private val format: String,
) : EventTracker() {

    override fun transactionMessage() = DatabaseEventApi(
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

    fun stopped() {
        end = LocalDateTime.now()
        result = EventResultApi.Success
    }

    override fun transactionMessage() = PortForwardEventApi(
        project = portForwardTarget.project,
        service = portForwardTarget.svc,
        port = portForwardTarget.port,
        startTime = start!!,
        endTime = end!!,
        eventType = type!!,
        result = result!!,
    )
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

    abstract fun transactionMessage(): EventApi
}