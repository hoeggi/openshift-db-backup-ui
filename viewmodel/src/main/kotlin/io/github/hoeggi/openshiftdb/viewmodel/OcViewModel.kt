package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.getOrDefault
import io.github.hoeggi.openshiftdb.api.response.*
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory

enum class LoginState {
    UNCHECKED, LOGGEDIN, NOT_LOGGEDIN
}

data class PortForwardTarget(val project: String, val svc: String, val port: Int)

class OcViewModel(port: Int, coroutineScope: CoroutineScope, errorViewer: ErrorViewer) :
    BaseViewModel(port, coroutineScope, errorViewer) {

    private val logger = LoggerFactory.getLogger(OcViewModel::class.java)

    private val _projects: MutableStateFlow<List<ProjectApi>> =
        MutableStateFlow(listOf())
    private val _currentProject: MutableStateFlow<ProjectApi> =
        MutableStateFlow(ProjectApi())
    private val _version: MutableStateFlow<VersionApi> =
        MutableStateFlow(VersionApi())
    private val _services: MutableStateFlow<List<ServicesApi>> =
        MutableStateFlow(listOf())
    private val _portForward: MutableStateFlow<Map<PortForwardTarget, List<PortForwardMessage>>> =
        MutableStateFlow(mapOf())

    private val _loginState: MutableStateFlow<LoginState> =
        MutableStateFlow(LoginState.UNCHECKED)
    private val _server: MutableStateFlow<List<ClusterApi>> =
        MutableStateFlow(listOf())

    fun update() {
        version()
        projects()
        currentProject()
        services()
    }

    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    fun checkLoginState() = coroutineScope.launch {
        val checkLogin = ocApi.checkLogin()
        _loginState.value = if (checkLogin.isSuccess) LoginState.LOGGEDIN else LoginState.NOT_LOGGEDIN
    }

    fun login(token: String, server: String) = coroutineScope.launch {
        val login = ocApi.login(token, server)
        _loginState.value = if (login.isSuccess) LoginState.LOGGEDIN else LoginState.NOT_LOGGEDIN
    }

    val server: StateFlow<List<ClusterApi>> = _server.asStateFlow()
    fun listServer() = coroutineScope.launch {
        _server.value = ocApi.server().getOrDefault(listOf())
    }

    fun switchProject(project: String) = coroutineScope.launch {
        val switchProject = ocApi.switchProject(project)
        _currentProject.value = switchProject.getOrDefault(ProjectApi())
        if (switchProject.isSuccess) {
            _services.value = ocApi.services().getOrDefault(listOf())
        }
    }

    val projects: StateFlow<List<ProjectApi>> = _projects.asStateFlow()
    private fun projects() = coroutineScope.launch {
        _projects.value = ocApi.projects().getOrDefault(listOf())
            .filterNot { it.name.startsWith("openshift") or it.name.startsWith("kube") }
    }

    val currentProject: StateFlow<ProjectApi> = _currentProject.asStateFlow()
    private fun currentProject() = coroutineScope.launch {
        _currentProject.value = ocApi.currentProject().getOrDefault(ProjectApi())
    }

    val version: StateFlow<VersionApi> = _version.asStateFlow()
    private fun version() = coroutineScope.launch {
        _version.value = ocApi.version().getOrDefault(VersionApi())
    }

    val services: StateFlow<List<ServicesApi>> = _services.asStateFlow()
    private fun services() = coroutineScope.launch {
        _services.value = ocApi.services().getOrDefault(listOf())
    }

    private val openPortForwars = mutableMapOf<PortForwardTarget, Job>()
    val portForward: StateFlow<Map<PortForwardTarget, List<PortForwardMessage>>> = _portForward.asStateFlow()
    fun portForward(svc: String, port: Int) {
        portForward(PortForwardTarget(currentProject.value.name, svc, port))
    }

    fun portForward(target: PortForwardTarget) {
        val launch = coroutineScope.launch(Dispatchers.IO) {
            val flow: Flow<PortForwardMessage> = ocApi.portForward(target.project, target.svc, target.port)
            flow.collect {
                ensureActive()
                logger.debug("message from port-forward: $it")
                val map = _portForward.value.toMutableMap()
                val list = map.getOrDefault(target, listOf()).toMutableList()
                list.add(it)
                map[target] = list
                _portForward.value = map
            }
        }
        openPortForwars.put(target, launch)
    }

    fun closePortForward(target: PortForwardTarget) {
        openPortForwars.remove(target)?.cancel()
        _portForward.value = _portForward.value.toMutableMap().apply {
            remove(target)
        }
    }

}