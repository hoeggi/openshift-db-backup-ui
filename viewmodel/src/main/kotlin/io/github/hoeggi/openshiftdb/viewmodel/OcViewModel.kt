package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.Api
import io.github.hoeggi.openshiftdb.api.OcApi
import io.github.hoeggi.openshiftdb.api.response.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory

enum class LoginState {
    UNCHECKED, LOGGEDIN, NOT_LOGGEDIN
}

data class PortForwardTarget(val project: String, val svc: String, val port: Int)

class OcViewModel(port: Int) {
    private val api: OcApi = Api(port)

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

    suspend fun update() {
        version()
        projects()
        currentProject()
        services()
    }

    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    suspend fun checkLoginState() {
        val checkLogin = api.checkLogin()
        _loginState.value = if (checkLogin.isSuccess) LoginState.LOGGEDIN else LoginState.NOT_LOGGEDIN
    }

    suspend fun login(token: String, server: String) {
        val login = api.login(token, server)
        _loginState.value = if (login.isSuccess) LoginState.LOGGEDIN else LoginState.NOT_LOGGEDIN
    }

    val server: StateFlow<List<ClusterApi>> = _server.asStateFlow()
    suspend fun listServer() {
        _server.value = api.server().getOrDefault(listOf())
    }

    val projects: StateFlow<List<ProjectApi>> = _projects.asStateFlow()
    suspend fun projects() {
        _projects.value = api.projects().getOrDefault(listOf())
    }

    val currentProject: StateFlow<ProjectApi> = _currentProject.asStateFlow()
    suspend fun currentProject() {
        _currentProject.value = api.currentProject().getOrDefault(ProjectApi())
    }

    suspend fun switchProject(project: String) {
        val switchProject = api.switchProject(project)
        _currentProject.value = switchProject.getOrDefault(ProjectApi())
        if (switchProject.isSuccess) {
            _services.value = api.services().getOrDefault(listOf())
        }
    }

    val version: StateFlow<VersionApi> = _version.asStateFlow()
    suspend fun version() {
        _version.value = api.version().getOrDefault(VersionApi())
    }

    val services: StateFlow<List<ServicesApi>> = _services.asStateFlow()
    suspend fun services() {
        _services.value = api.services().getOrDefault(listOf())
    }

    private val openPortForwars = mutableMapOf<PortForwardTarget, Job>()
    val portForward: StateFlow<Map<PortForwardTarget, List<PortForwardMessage>>> = _portForward.asStateFlow()
    suspend fun portForward(svc: String, port: Int) {
        portForward(PortForwardTarget(currentProject.value.name, svc, port))
    }

    suspend fun portForward(target: PortForwardTarget) {
        withContext(Dispatchers.IO) {
            val launch = launch {
                val flow: Flow<PortForwardMessage> = api.portForward(target.project, target.svc, target.port)
                flow.collect {
                    ensureActive()
                    logger.debug("message from port-forward: $it")
                    _portForward.value = _portForward.value.toMutableMap().apply {
                        putIfAbsent(target, listOf())!!.toMutableList().apply {
                            add(it)
                        }

                    }
                }
            }
            openPortForwars.put(target, launch)
        }
    }

    fun closePortForward(project: String, svc: String, port: Int) {
        closePortForward(PortForwardTarget(project, svc, port))
    }

    fun closePortForward(target: PortForwardTarget) {
        openPortForwars.remove(target)?.cancel()
    }

}