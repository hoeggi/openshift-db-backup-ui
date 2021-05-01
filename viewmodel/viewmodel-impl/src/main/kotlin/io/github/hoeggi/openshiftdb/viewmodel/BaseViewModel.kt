package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.Api
import io.github.hoeggi.openshiftdb.api.EventsApi
import io.github.hoeggi.openshiftdb.api.LoggingApi
import io.github.hoeggi.openshiftdb.api.OcApi
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import org.slf4j.LoggerFactory
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private data class ViewModelFactoryImpl(
    private val _ocViewModel: Lazy<OcViewModel>,
    private val _postgresViewModel: Lazy<PostgresViewModel>,
    private val _eventsViewModel: Lazy<EventsViewModel>,
    private val _syslogViewModel: Lazy<SyslogViewModel>,
) : ViewModelFactory {
    override val ocViewModel by _ocViewModel
    override val postgresViewModel by _postgresViewModel
    override val eventsViewModel by _eventsViewModel
    override val syslogViewModel by _syslogViewModel
}

fun viewModels(
    port: Int,
    errorViewer: ErrorViewer,
): ViewModelFactory = ViewModelFactoryImpl(
    _ocViewModel = lazy { OcViewModelImpl(port, errorViewer) },
    _postgresViewModel = lazy { PostgresViewModelImpl(port, errorViewer) },
    _eventsViewModel = lazy { EventsViewModelImpl(port, errorViewer) },
    _syslogViewModel = lazy { SyslogViewModelImpl(port, errorViewer) },
)

internal abstract class BaseViewModelImpl(port: Int, private val errorViewer: ErrorViewer) : BaseViewModel {
    internal val logger = LoggerFactory.getLogger(this.javaClass)
    private val api: Api = Api(port)

    internal val ocApi: OcApi
        get() = api
    internal val postgresApi: PostgresApi
        get() = api
    internal val eventsApi: EventsApi
        get() = api
    internal val loggingApi: LoggingApi
        get() = api

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler(errorViewer))
    override val coroutineScope
        get() = scope + SupervisorJob()

    val showWarning: (Throwable) -> Unit = {
        logger.warn("warning", it)
        showWarning(it.message)
    }

    fun showWarning(message: String?) {
        message?.run {
            errorViewer.showError(errorViewer.warning(this))
        }
    }
}

private class CoroutineExceptionHandler(private val errorViewer: ErrorViewer) :
    AbstractCoroutineContextElement(kotlinx.coroutines.CoroutineExceptionHandler),
    kotlinx.coroutines.CoroutineExceptionHandler {
    private val logger = LoggerFactory.getLogger(kotlinx.coroutines.CoroutineExceptionHandler::class.java)
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error("error in coroutine", exception)
        errorViewer.showError(
            object : ErrorViewer.Error {
                override val thread = Thread.currentThread()
                override val throwable = exception
                override val fired = true
            }
        )
    }
}
