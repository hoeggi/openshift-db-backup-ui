package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.Api
import io.github.hoeggi.openshiftdb.api.OcApi
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.errorhandler.CoroutineExceptionHandler
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.plus

data class ViewModels(val ocViewModel: OcViewModel, val postgresViewModel: PostgresViewModel)


fun viewModels(
    port: Int,
    coroutineScope: CoroutineScope,
    errorViewer: ErrorViewer,
) = ViewModels(
    ocViewModel = OcViewModel(port, coroutineScope, errorViewer),
    postgresViewModel = PostgresViewModel(port, coroutineScope, errorViewer)
)

abstract class BaseViewModel(port: Int, private val scope: CoroutineScope, errorViewer: ErrorViewer) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler(errorViewer)
    private val api: Api = Api(port)

    protected val ocApi: OcApi
        get() = api
    protected val postgresApi: PostgresApi
        get() = api

    val coroutineScope
        get() = scope + Job() + coroutineExceptionHandler

}