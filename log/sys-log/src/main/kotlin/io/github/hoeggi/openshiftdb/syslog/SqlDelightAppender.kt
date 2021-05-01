package io.github.hoeggi.openshiftdb.syslog

import ch.qos.logback.classic.db.DBHelper
import ch.qos.logback.classic.spi.CallerData
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.UnsynchronizedAppenderBase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map

data class LoggingEvent(
    val event: Logging_event,
    val properties: List<Logging_event_property>,
    val exception: List<Logging_event_exception>
)

object SyslogQuerier {
    private val syslogDb by SyslogDbInitializer()

    fun events() = syslogDb.logEventQueries
        .logEvents()
        .asFlow()
        .mapToList()
        .map {
            it.pmap {
                LoggingEvent(
                    it,
                    syslogDb.logEventQueries.logProperties(it.event_id).executeAsList(),
                    syslogDb.logEventQueries.logException(it.event_id).executeAsList()
                )
            }
        }

    suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
        map { async { f(it) } }.awaitAll()
    }
}

private object SyslogDbInitializer {
    val db = lazy {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            .let {
                SyslogDb.Schema.create(it)
                SyslogDb(it)
            }
    }

    operator fun invoke() = db
}

internal class SqlDelightAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {
    private val syslogDb by SyslogDbInitializer()

    override fun append(event: ILoggingEvent) {
        val arguments = event.argumentArray
            ?.mapIndexed { idx, elem -> idx to elem?.toString() }
            ?.toMap() ?: mapOf()

        val caller = extractFirstCaller(event.callerData)
        syslogDb.run {
            logEventQueries.transaction {
                logEventQueries.insertEvent(
                    event.timeStamp,
                    event.formattedMessage,
                    event.loggerName,
                    event.level.levelStr,
                    event.threadName,
                    DBHelper.computeReferenceMask(event).toLong(),
                    arguments[0],
                    arguments[1],
                    arguments[2],
                    arguments[3],
                    "${caller?.fileName}",
                    "${caller?.className}",
                    "${caller?.methodName}",
                    "${caller?.lineNumber}"
                )
                val eventId = logEventQueries.selectLastInserted().executeAsOne()
                val mergedMap = mergePropertyMaps(event)
                mergedMap.forEach { (k, v) ->
                    logEventQueries.insertProperty(eventId, k, v)
                }
                insertThrowable(event.throwableProxy, logEventQueries, eventId)
            }
        }
    }

    private fun insertThrowable(proxy: IThrowableProxy?, logEventQueries: LogEventQueries, eventId: Long) {
        var tp = proxy
        var baseIndex: Long = 0
        while (tp != null) {
            baseIndex = buildExceptionStatement(tp, baseIndex, logEventQueries, eventId)
            tp = tp.cause
        }
    }

    private fun buildExceptionStatement(
        tp: IThrowableProxy,
        baseIndex: Long,
        logEventQueries: LogEventQueries,
        eventId: Long
    ): Long {
        var idx = baseIndex
        val buf = StringBuilder()
        ThrowableProxyUtil.subjoinFirstLine(buf, tp)
        logEventQueries.insertException(eventId, idx++, buf.toString())
        val commonFrames = tp.commonFrames
        val stepArray = tp.stackTraceElementProxyArray
        for (i in 0 until stepArray.size - commonFrames) {
            val sb = StringBuilder()
            sb.append(CoreConstants.TAB)
            ThrowableProxyUtil.subjoinSTEP(sb, stepArray[i])
            logEventQueries.insertException(eventId, idx++, sb.toString())
        }
        if (commonFrames > 0) {
            val sb = StringBuilder()
            sb.append(CoreConstants.TAB).append("... ").append(commonFrames).append(" common frames omitted")
            logEventQueries.insertException(eventId, idx++, sb.toString())
        }
        return idx
    }

    private fun mergePropertyMaps(event: ILoggingEvent): Map<String, String> =
        mutableMapOf<String, String>().apply {
            putAll(event.loggerContextVO.propertyMap ?: emptyMap())
            putAll(event.mdcPropertyMap ?: emptyMap())
        }

    private fun extractFirstCaller(callerDataArray: Array<StackTraceElement?>?) =
        callerDataArray?.firstOrNull() ?: EMPTY_CALLER_DATA

    companion object {
        val EMPTY_CALLER_DATA = CallerData.naInstance()
    }
}
