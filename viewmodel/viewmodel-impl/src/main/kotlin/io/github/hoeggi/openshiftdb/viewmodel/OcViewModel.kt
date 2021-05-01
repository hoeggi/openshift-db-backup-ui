package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.getOrDefault
import io.github.hoeggi.openshiftdb.api.onFailure
import io.github.hoeggi.openshiftdb.api.onSuccess
import io.github.hoeggi.openshiftdb.api.response.ClusterApi
import io.github.hoeggi.openshiftdb.api.response.FullContext
import io.github.hoeggi.openshiftdb.api.response.ProjectApi
import io.github.hoeggi.openshiftdb.api.response.SwitchContextApi
import io.github.hoeggi.openshiftdb.api.response.VersionApi
import io.github.hoeggi.openshiftdb.api.response.isSameCluster
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.viewmodel.models.Context
import io.github.hoeggi.openshiftdb.viewmodel.models.LoginState
import io.github.hoeggi.openshiftdb.viewmodel.models.OpenPortForward
import io.github.hoeggi.openshiftdb.viewmodel.models.PortForwardMessage
import io.github.hoeggi.openshiftdb.viewmodel.models.PortForwardMessage.Companion.portForwardMessage
import io.github.hoeggi.openshiftdb.viewmodel.models.PortForwardTarget
import io.github.hoeggi.openshiftdb.viewmodel.models.Service
import io.github.hoeggi.openshiftdb.viewmodel.models.toContext
import io.github.hoeggi.openshiftdb.viewmodel.models.toService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

internal class OcViewModelImpl constructor(port: Int, errorViewer: ErrorViewer) :
    BaseViewModelImpl(port, errorViewer), OcViewModel {

    private val _projects: MutableStateFlow<List<String>> =
        MutableStateFlow(listOf())
    private val _currentProject: MutableStateFlow<String> =
        MutableStateFlow("")
    private val _version: MutableStateFlow<VersionApi> =
        MutableStateFlow(VersionApi())
    private val _services: MutableStateFlow<List<Service>> =
        MutableStateFlow(listOf())
    private val portForwardData =
        mutableMapOf<PortForwardTarget, MutableList<PortForwardMessage>>()
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
            val eventTracker = PortForwardEventTracker(target)
            val flow = ocApi.portForward(target.project, target.svc, target.port)
            flow.onCompletion {
                logger.debug("port-forward completed")
                coroutineScope.launch(Dispatchers.IO) {
                    eventTracker.stopped()
                    sendPortForwardEvent(eventTracker)
                }
            }.collect {
                trackPortforwardMessages(it, eventTracker) {
                    portForwardData[target] = portForwardData.getOrDefault(target, mutableListOf()).apply {
                        add(portForwardMessage(it))
                    }
                    _portForward.value = portForwardData.map {
                        OpenPortForward(it.key, it.value)
                    }
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

    private suspend inline fun trackPortforwardMessages(
        message: io.github.hoeggi.openshiftdb.api.response.PortForwardMessage,
        eventTracker: PortForwardEventTracker,
        crossinline action: suspend (value: io.github.hoeggi.openshiftdb.api.response.PortForwardMessage) -> Unit,
    ) = coroutineScope {
        eventTracker.trackMessage(message)
        if (message is io.github.hoeggi.openshiftdb.api.response.PortForwardMessage.Start) {
            sendPortForwardEvent(eventTracker)
        }
        ensureActive()
        action(message)
        if (message is io.github.hoeggi.openshiftdb.api.response.PortForwardMessage.CloseMessage) {
            cancel("closed")
        }
    }

    private suspend fun sendPortForwardEvent(tracker: PortForwardEventTracker) {
        val newTransaction = eventsApi.newEvent(tracker.event())
        newTransaction.onSuccess {
            logger.debug("tracked port-forward finished $it")
        }.onFailure {
            logger.error("error tracking port-forward finished", it)
        }
    }
}
