package io.github.hoeggi.openshiftdb.errorhandler

import kotlinx.coroutines.CoroutineExceptionHandler
import org.slf4j.LoggerFactory
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


interface ErrorViewer {
    fun showError(error: Message)
    fun showWarning(warning: Message)

    interface Message {
        val fired: Boolean
    }

    interface Error : Message {
        val thread: Thread?
        val throwable: Throwable?
    }

    interface Warning : Message {
        val message: String
    }

    fun empty() = object : Message {
        override val fired = false
    }

    fun warning(message: String): Warning = Warning(message, true)
    fun error(
        thread: Thread? = Thread.currentThread(),
        throwable: Throwable? = Throwable(),
    ): Error = Error(thread, throwable, true)

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

class ExceptionHandler(
    private val errorViewer: ErrorViewer,
    private val defaultExceptionHander: Thread.UncaughtExceptionHandler,
) : Thread.UncaughtExceptionHandler {
    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        errorViewer.showError(errorViewer.error(t, e))
        defaultExceptionHander.uncaughtException(t, e)
    }
}

private data class Warning(
    override val message: String,
    override val fired: Boolean,
) : ErrorViewer.Warning

private data class Error(
    override val thread: Thread?,
    override val throwable: Throwable?,
    override val fired: Boolean,
) : ErrorViewer.Error