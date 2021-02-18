package io.github.hoeggi.openshiftdb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

@Composable
fun <T : R, R> Flow<T>.collectAsStateFLowState(
    initial: R,
    context: CoroutineContext = EmptyCoroutineContext
): State<R> {
    val init = when (this) {
        is StateFlow -> when (value) {
            null -> initial
            else -> value
        }
        else -> initial
    }
    return produceState(init, this, context) {
        if (context == EmptyCoroutineContext) {
            collect { value = it }
        } else withContext(context) {
            collect { value = it }
        }
    }
}

class ExceptionHandler(private val navigationViewModel: GlobalState) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        printlnErr("$t\n${e?.stackTraceToString()}")
        navigationViewModel.showError(Error(t, e, true))
    }
}