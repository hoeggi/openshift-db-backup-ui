package io.github.hoeggi.openshiftdb

import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import java.util.concurrent.Executors
import io.github.hoeggi.openshiftdb.ui.composables.Error

fun exception(
    delay: Long = 1000,
    throwable: Throwable = RuntimeException("test", RuntimeException("test2", RuntimeException("test3")))
) {
    Executors.newSingleThreadExecutor().execute {
        Thread.sleep(delay)
        throw throwable
    }
}

fun printlnErr(line: String) {
    System.err.println(line)
}

class ExceptionHandler(private val navigationViewModel: GlobalState) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        printlnErr("$t\n${e?.stackTraceToString()}")
        navigationViewModel.showError(Error(t, e, true))
    }
}