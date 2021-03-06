package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.Api
import io.github.hoeggi.openshiftdb.api.OcApi
import io.github.hoeggi.openshiftdb.api.PostgresApi
import kotlinx.coroutines.CoroutineScope

data class ViewModels(val ocViewModel: OcViewModel, val postgresViewModel: PostgresViewModel)

fun viewModels(port: Int, coroutineScope: CoroutineScope) = ViewModels(
    ocViewModel = OcViewModel(port, coroutineScope),
    postgresViewModel = PostgresViewModel(port, coroutineScope)
)

abstract class BaseViewModel(port: Int, protected val coroutineScope: CoroutineScope) {
    private val api: Api = Api(port)
    protected val ocApi: OcApi
        get() = api
    protected val postgresApi: PostgresApi
        get() = api
}