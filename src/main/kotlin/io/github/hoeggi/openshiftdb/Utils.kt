package io.github.hoeggi.openshiftdb

import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

fun exception(
    delay: Long = 1000,
    throwable: Throwable = RuntimeException("test", RuntimeException("test2", RuntimeException("test3"))),
) {
    Executors.newSingleThreadExecutor().execute {
        Thread.sleep(delay)
        throw throwable
    }
}

fun printlnErr(line: String) {
    System.err.println(line)
}
