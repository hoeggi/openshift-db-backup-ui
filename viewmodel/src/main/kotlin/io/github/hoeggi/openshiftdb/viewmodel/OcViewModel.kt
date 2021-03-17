package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.getOrDefault
import io.github.hoeggi.openshiftdb.api.onFailure
import io.github.hoeggi.openshiftdb.api.onSuccess
import io.github.hoeggi.openshiftdb.api.response.*
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.viewmodel.models.*
import io.github.hoeggi.openshiftdb.viewmodel.models.PortForwardMessage
import io.github.hoeggi.openshiftdb.viewmodel.models.PortForwardMessage.Companion.portForwardMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect


interface OcViewModel: ViewModel {
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

internal class OcViewModelImpl internal constructor(port: Int, coroutineScope: CoroutineScope, errorViewer: ErrorViewer) :
    BaseViewModel(port, coroutineScope, errorViewer), OcViewModel {

    private val _projects: MutableStateFlow<List<String>> =
        MutableStateFlow(listOf())
    private val _currentProject: MutableStateFlow<String> =
        MutableStateFlow("")
    private val _version: MutableStateFlow<VersionApi> =
        MutableStateFlow(VersionApi())
    private val _services: MutableStateFlow<List<Service>> =
        MutableStateFlow(listOf())
    private val portForwardData = mutableMapOf<PortForwardTarget, MutableList<PortForwardMessage>>()
    private val _portForward: MutableStateFlow<List<OpenPortForward>> =
        MutableStateFlow(listOf())

    private val _loginState: MutableStateFlow<LoginState> =
        MutableStateFlow(LoginState.UNCHECKED)
    private val _server: MutableStateFlow<List<ClusterApi>> =
        MutableStateFlow(listOf())

    private val _context: MutableStateFlow<Context> =
        MutableStateFlow(Context("", listOf()))

    override fun update() {
        version()
        projects()
        currentProject()
        services()
        context()
    }

    override val context: StateFlow<Context> = _context.asStateFlow()
    override fun context() = coroutineScope.launch {
        val context = ocApi.context()
        context.onSuccess {
            _context.value = it.toContext()
        }.onFailure {
            showWarning(it)
        }
    }

    override fun switchContext(newContext: FullContext) = coroutineScope.launch {
        val currentContext = ocApi.switchContext(SwitchContextApi(newContext))
        if (!newContext.isSameCluster(context.value.current)) closeAllPortForward()
        currentContext.onSuccess {
            _context.value = context.value.copy(current = it.context)
            checkLoginState()
            update()
        }.onFailure(showWarning)
    }

    override val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    override fun checkLoginState() = coroutineScope.launch {
        val checkLogin = ocApi.checkLogin()
        checkLogin.onSuccess {
            _loginState.value = LoginState.LOGGEDIN
        }.onFailure {
            showWarning(it)
            _loginState.value = LoginState.NOT_LOGGEDIN
        }
    }

    override fun login(token: String, server: String) = coroutineScope.launch {
        val login = ocApi.login(token, server)
        login.onSuccess {
            _loginState.value = LoginState.LOGGEDIN
        }.onFailure {
            showWarning(it)
            _loginState.value = LoginState.NOT_LOGGEDIN
        }
    }

    override val server = _server.asStateFlow()
    override fun listServer() = coroutineScope.launch {
        _server.value = ocApi.server().onFailure {
            showWarning(it)
        }.getOrDefault(listOf())
    }

    override fun switchProject(project: String) = coroutineScope.launch {
        val switchProject = ocApi.switchProject(project)

        _currentProject.value = switchProject.onFailure {
            showWarning(it)
        }.getOrDefault(ProjectApi()).name

        if (switchProject.isSuccess) {
            services()
        }
    }

    override val projects: StateFlow<List<String>> = _projects.asStateFlow()
    private fun projects() = coroutineScope.launch {
        val projects = ocApi.projects()
        _projects.value = projects.onFailure {
            showWarning(it)
        }.getOrDefault(listOf())
            .filterNot { it.name.startsWith("openshift") or it.name.startsWith("kube") }
            .map { it.name }
    }

    override val currentProject: StateFlow<String> = _currentProject.asStateFlow()
    private fun currentProject() = coroutineScope.launch {
        _currentProject.value = ocApi.currentProject().onFailure {
            showWarning(it)
        }.getOrDefault(ProjectApi()).name
    }

    override val version = _version.asStateFlow()
    private fun version() = coroutineScope.launch {
        _version.value = ocApi.version().onFailure {
            showWarning(it)
        }.getOrDefault(VersionApi())
    }

    override val services = _services.asStateFlow()
    private fun services() = coroutineScope.launch {
        _services.value = ocApi.services().onFailure {
            if (it.message?.startsWith("404") != true) showWarning(it)
        }.getOrDefault(listOf()).map { it.toService() }
    }

    private val openPortForwards = mutableMapOf<PortForwardTarget, Job>()
    override val portForward = _portForward.asStateFlow()

    override fun portForward(svc: String, port: Int) {
        portForward(PortForwardTarget(currentProject.value, svc, port))
    }

    override fun portForward(target: PortForwardTarget) {
        val launch = coroutineScope.launch(Dispatchers.IO) {
            val flow = ocApi.portForward(target.project, target.svc, target.port)
            flow.collect { message ->
                ensureActive()
                portForwardData[target] = portForwardData.getOrDefault(target, mutableListOf()).apply {
                    add(portForwardMessage(message))
                }
                _portForward.value = portForwardData.map {
                    OpenPortForward(it.key, it.value)
                }
            }
        }
        openPortForwards[target] = launch
    }

    override fun closePortForward(target: PortForwardTarget) {
        openPortForwards.remove(target)?.cancel()
        portForwardData.remove(target)
        _portForward.value = portForwardData.map { OpenPortForward(it.key, it.value) }
    }

    override fun closeAllPortForward() {
        openPortForwards.onEach {
            it.value.cancel()
        }.clear()
        portForwardData.clear()
        _portForward.value = listOf()
    }
}