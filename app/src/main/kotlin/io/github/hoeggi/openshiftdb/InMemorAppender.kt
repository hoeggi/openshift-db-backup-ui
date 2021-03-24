package io.github.hoeggi.openshiftdb

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ANSIConstants
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase

class HighlightingCompositeConverterEx : ForegroundCompositeConverterBase<ILoggingEvent>() {
    override fun getForegroundColorCode(event: ILoggingEvent): String {
        val level: Level = event.level
        return when (level.toInt()) {
            Level.ERROR_INT -> ANSIConstants.RED_FG // same as default color scheme
            Level.WARN_INT -> ANSIConstants.YELLOW_FG // same as default color scheme
            Level.INFO_INT -> ANSIConstants.BLUE_FG // use CYAN instead of BLUE
            Level.DEBUG_INT -> ANSIConstants.CYAN_FG // use CYAN instead of BLUE
            else -> ANSIConstants.DEFAULT_FG
        }
    }
}
//
// object MemoryAppenderAccess {
//    fun asText(): String {
//        val instance = instance ?: return ""
//        val sb = StringBuilder()
//        for (e in instance.getEvents()) {
//            sb.append(e)
//            sb.append('\n')
//        }
//        return sb.toString()
//    }
// }
//
// object MemoryAppenderInstance {
//    var instance: MemoryAppender? = null
// }
//
// class MemoryAppender : AppenderBase<ILoggingEvent>() {
//    var limit = 150
//    private val events: MutableList<ILoggingEvent> = ArrayList()
//    override fun start() {
//        super.start()
//        instance = this
//    }
//
//    override fun stop() {
//        instance = null
//        super.stop()
//        events.clear()
//    }
//
//    override fun append(e: ILoggingEvent) {
//        synchronized(events) {
//            events.add(e)
//            if (events.size > limit && limit > 0) {
//                events.removeAt(0)
//            }
//        }
//    }
//
//    fun getEvents(): List<ILoggingEvent> {
//        val retVal: MutableList<ILoggingEvent> = ArrayList()
//        synchronized(events) { retVal.addAll(events) }
//        return retVal
//    }
// }
