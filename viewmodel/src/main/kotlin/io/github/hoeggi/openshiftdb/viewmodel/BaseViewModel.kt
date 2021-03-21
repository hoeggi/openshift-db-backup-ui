package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.Api
import io.github.hoeggi.openshiftdb.api.EventsApi
import io.github.hoeggi.openshiftdb.api.OcApi
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.errorhandler.CoroutineExceptionHandler
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import org.slf4j.LoggerFactory

data class ViewModelProvider(
    private val _ocViewModel: Lazy<OcViewModel>,
    private val _postgresViewModel: Lazy<PostgresViewModel>,
    private val _eventsViewModel: Lazy<EventsViewModel>,
) {
    val ocViewModel by _ocViewModel
    val postgresViewModel by _postgresViewModel
    val eventsViewModel by _eventsViewModel
}


fun viewModels(
    port: Int,
    errorViewer: ErrorViewer,
) = ViewModelProvider(
    _ocViewModel = lazy { OcViewModel(port, errorViewer) },
    _postgresViewModel = lazy { PostgresViewModel(port, errorViewer) },
    _eventsViewModel = lazy { EventsViewModel(port, errorViewer) },
)

abstract class BaseViewModel(port: Int, private val errorViewer: ErrorViewer) {
    internal val logger = LoggerFactory.getLogger(this.javaClass)
    private val api: Api = Api(port)

    internal val ocApi: OcApi
        get() = api
    internal val postgresApi: PostgresApi
        get() = api
    internal val eventsApi: EventsApi
        get() = api

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler(errorViewer))
    val coroutineScope
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