package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.response.ClusterApi
import io.github.hoeggi.openshiftdb.api.response.FullContext
import io.github.hoeggi.openshiftdb.api.response.VersionApi
import io.github.hoeggi.openshiftdb.viewmodel.models.Context
import io.github.hoeggi.openshiftdb.viewmodel.models.LoginState
import io.github.hoeggi.openshiftdb.viewmodel.models.OpenPortForward
import io.github.hoeggi.openshiftdb.viewmodel.models.PortForwardTarget
import io.github.hoeggi.openshiftdb.viewmodel.models.Service
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

interface OcViewModel : BaseViewModel {
    val context: StateFlow<Context>
    val loginState: StateFlow<LoginState>
    val server: StateFlow<List<ClusterApi>>
    val projects: StateFlow<List<String>>
    val currentProject: StateFlow<String>
    val version: StateFlow<VersionApi>
    val services: StateFlow<List<Service>>
    val portForward: StateFlow<List<OpenPortForward>>
    fun update()
    fun context(): Job
    fun switchContext(newContext: FullContext): Job
    fun checkLoginState(): Job
    fun login(token: String, server: String): Job
    fun listServer(): Job
    fun switchProject(project: String): Job
    fun portForward(svc: String, port: Int)
    fun portForward(target: PortForwardTarget)
    fun closePortForward(target: PortForwardTarget)
    fun closeAllPortForward()
}
