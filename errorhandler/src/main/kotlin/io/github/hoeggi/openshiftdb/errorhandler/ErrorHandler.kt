package io.github.hoeggi.openshiftdb.errorhandler

import kotlinx.coroutines.CoroutineExceptionHandler
import org.slf4j.LoggerFactory
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


interface ErrorViewer {
    fun showError(e: Error)
    interface Error {
        val thread: Thread?
        val throwable: Throwable?
        val fired: Boolean
    }

    fun error(
        thread: Thread? = Thread.currentThread(),
        throwable: Throwable? = Throwable(),
        fired: Boolean = false,
    ): Error = Error(thread, throwable, fired)

}

class CoroutineExceptionHandler(private val errorViewer: ErrorViewer) :
    AbstractCoroutineContextElement(CoroutineExceptionHandler),
    CoroutineExceptionHandler {
    private val logger = LoggerFactory.getLogger(CoroutineExceptionHandler::class.java)
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error("error in coroutine", exception)
        errorViewer.showError(object : ErrorViewer.Error {
            override val thread = Thread.currentThread()
            override val throwable = exception
            override val fired = true
        })
    }
}

class ExceptionHandler(private val errorViewer: ErrorViewer) : Thread.UncaughtExceptionHandler {
    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        logger.error("$t", e)
        errorViewer.showError(errorViewer.error(t, e, true))
    }
}


private data class Error(
    override val thread: Thread?,
    override val throwable: Throwable?,
    override val fired: Boolean,
) : ErrorViewer.Error