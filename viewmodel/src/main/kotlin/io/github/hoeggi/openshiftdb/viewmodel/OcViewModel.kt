package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.getOrDefault
import io.github.hoeggi.openshiftdb.api.onFailure
import io.github.hoeggi.openshiftdb.api.onSuccess
import io.github.hoeggi.openshiftdb.api.response.*
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class LoginState {
    UNCHECKED, LOGGEDIN, NOT_LOGGEDIN
}

data class PortForwardTarget(val project: String, val svc: String, val port: Int)

class OcViewModel(port: Int, coroutineScope: CoroutineScope, errorViewer: ErrorViewer) :
    BaseViewModel(port, coroutineScope, errorViewer) {

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

    private val _context: MutableStateFlow<ContextApi> =
        MutableStateFlow(ContextApi("", listOf()))

    fun update() {
        version()
        projects()
        currentProject()
        services()
        context()
    }

    val context: StateFlow<ContextApi> = _context.asStateFlow()
    fun context() = coroutineScope.launch {
        val context = ocApi.context()
        context.onSuccess {
            _context.value = it
        }.onFailure {
            showWarning(it)
        }
    }

    fun switchContext(newContext: FullContext) = coroutineScope.launch {
        val currentContext = ocApi.switchContext(SwitchContextApi(newContext))
        if (!newContext.isSameCluster(context.value.current)) closeAllPortForward()
        currentContext.onSuccess {
            _context.value = context.value.copy(current = it.context)
            checkLoginState()
            update()
        }.onFailure(showWarning)
    }

    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    fun checkLoginState() = coroutineScope.launch {
        val checkLogin = ocApi.checkLogin()
        checkLogin.onSuccess {
            _loginState.value = LoginState.LOGGEDIN
        }.onFailure {
            showWarning(it)
            _loginState.value = LoginState.NOT_LOGGEDIN
        }
    }

    fun login(token: String, server: String) = coroutineScope.launch {
        val login = ocApi.login(token, server)
        login.onSuccess {
            _loginState.value = LoginState.LOGGEDIN
        }.onFailure {
            showWarning(it)
            _loginState.value = LoginState.NOT_LOGGEDIN
        }
    }

    val server: StateFlow<List<ClusterApi>> = _server.asStateFlow()
    fun listServer() = coroutineScope.launch {
        _server.value = ocApi.server().onFailure {
            showWarning(it)
        }.getOrDefault(listOf())
    }

    fun switchProject(project: String) = coroutineScope.launch {
        val switchProject = ocApi.switchProject(project)

        _currentProject.value = switchProject.onFailure {
            showWarning(it)
        }.getOrDefault(ProjectApi())

        if (switchProject.isSuccess) {
            services()
        }
    }

    val projects: StateFlow<List<ProjectApi>> = _projects.asStateFlow()
    private fun projects() = coroutineScope.launch {
        val projects = ocApi.projects()
        _projects.value = projects.onFailure {
            showWarning(it)
        }.getOrDefault(listOf())
            .filterNot { it.name.startsWith("openshift") or it.name.startsWith("kube") }
    }

    val currentProject: StateFlow<ProjectApi> = _currentProject.asStateFlow()
    private fun currentProject() = coroutineScope.launch {
        _currentProject.value = ocApi.currentProject().onFailure {
            showWarning(it)
        }.getOrDefault(ProjectApi())
    }

    val version: StateFlow<VersionApi> = _version.asStateFlow()
    private fun version() = coroutineScope.launch {
        _version.value = ocApi.version().onFailure {
            showWarning(it)
        }.getOrDefault(VersionApi())
    }

    val services: StateFlow<List<ServicesApi>> = _services.asStateFlow()
    private fun services() = coroutineScope.launch {
        _services.value = ocApi.services().onFailure {
            if (it.message?.startsWith("404") != true) showWarning(it)
        }.getOrDefault(listOf())
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

    fun closeAllPortForward() {
        openPortForwars.onEach {
            it.value.cancel()
        }.clear()
        _portForward.value = mapOf()
    }
}