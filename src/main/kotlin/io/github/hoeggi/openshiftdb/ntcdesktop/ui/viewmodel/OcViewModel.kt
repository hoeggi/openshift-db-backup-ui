package io.github.hoeggi.openshiftdb.ntcdesktop.ui.viewmodel

import io.github.hoeggi.openshiftdb.ntcdesktop.process.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OcViewModel(private val oc: OC, private val coroutineScope: CoroutineScope) {

    private val _projects: MutableStateFlow<OC.OcResult> =
        MutableStateFlow(OC.OcResult.Unset)
    private val _currentProject: MutableStateFlow<OC.OcResult> =
        MutableStateFlow(OC.OcResult.Unset)
    private val _version: MutableStateFlow<OC.OcResult> =
        MutableStateFlow(OC.OcResult.Unset)
    private val _services: MutableStateFlow<OC.OcResult> =
        MutableStateFlow(OC.OcResult.Unset)
    private val _portForward: MutableStateFlow<OC.PortForward?> =
        MutableStateFlow(null)

    private val _loginState: MutableStateFlow<OC.OcResult.LoginState> =
        MutableStateFlow(OC.OcResult.LoginState.Unchecked)
    private val _server: MutableStateFlow<List<Server>> =
        MutableStateFlow(listOf())

    fun update() {
        version()
        projects()
        currentProject()
        services()
    }

    val loginState: StateFlow<OC.OcResult.LoginState> = _loginState.asStateFlow()
    fun checkLoginState() = coroutineScope.launch {
        _loginState.value = oc.checkLogin()
    }

    fun login(token: String, server: String) = coroutineScope.launch {
        _loginState.value = oc.login(token, server)
    }

    val server: StateFlow<List<Server>> = _server.asStateFlow()
    fun listServer() = coroutineScope.launch {
        _server.value = parseServer(oc.listServer().json)
    }

    val projects: StateFlow<OC.OcResult> = _projects.asStateFlow()
    fun projects() = coroutineScope.launch {
        _projects.value = oc.projects()
    }

    val currentProject: StateFlow<OC.OcResult> = _currentProject.asStateFlow()
    fun currentProject() = coroutineScope.launch {
        _currentProject.value = oc.project()
    }

    val version: StateFlow<OC.OcResult> = _version.asStateFlow()
    fun version() = coroutineScope.launch {
        _version.value = oc.version()
    }

    val services: StateFlow<OC.OcResult> = _services.asStateFlow()
    fun services() = coroutineScope.launch {
        _services.value = oc.services()
    }

    val portForward: StateFlow<OC.PortForward?> = _portForward.asStateFlow()
    fun portForward(project: String, name: String, port: String) = coroutineScope.launch {
        _portForward.value = oc.portForward(project, name, port)
    }

    fun closePortForward(portForward: OC.PortForward) {
        portForward.stop()
        _portForward.value = null
    }

    fun switchProject(project: String) = coroutineScope.launch {
        _currentProject.value = oc.switchProject(project)
        _services.value = oc.services()
    }
}