package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.Api
import io.github.hoeggi.openshiftdb.api.OcApi
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.api.response.*
import io.github.hoeggi.openshiftdb.errorhandler.CoroutineExceptionHandler
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.viewmodel.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.slf4j.LoggerFactory
import kotlin.coroutines.EmptyCoroutineContext

class ViewModels(val ocViewModel: OcViewModel, val postgresViewModel: PostgresViewModel)

fun viewModels(
    port: Int,
    coroutineScope: CoroutineScope,
    errorViewer: ErrorViewer,
) = ViewModels(
    ocViewModel = OcViewModelImpl(port, coroutineScope, errorViewer),
    postgresViewModel = PostgresViewModelImpl(port, coroutineScope, errorViewer),
)

interface ViewModel {
    val coroutineScope: CoroutineScope
    val showWarning: (Throwable) -> Unit
    fun showWarning(message: String?)
}

abstract class BaseViewModel(port: Int, private val scope: CoroutineScope, private val errorViewer: ErrorViewer) :
    ViewModel {
    internal val logger = LoggerFactory.getLogger(this.javaClass)
    private val coroutineExceptionHandler = CoroutineExceptionHandler(errorViewer)
    private val api: Api = Api(port)

    internal val ocApi: OcApi
        get() = api
    internal val postgresApi: PostgresApi
        get() = api

    override val coroutineScope
        get() = scope + coroutineExceptionHandler

    override val showWarning: (Throwable) -> Unit = {
        logger.warn("warning", it)
        showWarning(it.message)
    }

    override fun showWarning(message: String?) {
        message?.run {
            errorViewer.showError(errorViewer.warning(this))
        }
    }
}

val postgresDummy = object : PostgresViewModel {
    override val dumpPath = MutableStateFlow(System.getProperty("user.home"))
    override val password = MutableStateFlow("")
    override val userName = MutableStateFlow("postgres")
    override val secrets = MutableStateFlow(listOf<SecretsApi>())
    override val databases = MutableStateFlow("")
    override val databasesLines = MutableStateFlow(listOf<String>())
    override val postgresVersion = MutableStateFlow("")
    override val selectedDatabase = MutableStateFlow(-1)
    override val version = MutableStateFlow(ToolsVersionApi())
    override val restoreInfo = MutableStateFlow(listOf<String>())
    override val restorePath = MutableStateFlow("")
    override val restoreCommand = MutableStateFlow(RestoreCommandApi())
    override val downloadProgress: MutableStateFlow<List<DatabaseDownloadMessage.InProgressMessage>> =
        MutableStateFlow(listOf())
    override val downloadState: MutableStateFlow<DatabaseDownloadMessage> =
        MutableStateFlow(DatabaseDownloadMessage.unspecified())
    override val restoreState: MutableStateFlow<DatabaseRestoreMessage> =
        MutableStateFlow(DatabaseRestoreMessage.unspecified())
    override val restoreProgress: MutableStateFlow<List<DatabaseRestoreMessage.InProgressMessage>> =
        MutableStateFlow(listOf())


    override fun update() {}

    override fun dumpDatabase(database: String, format: String) {}

    override fun dumpPath(path: String) {}

    override fun updateSelectedDatabase(index: Int) {}

    override fun listLines() = coroutineScope.launch { }

    override fun clearDatabaseText() {}

    override fun listPretty() = coroutineScope.launch { }

    override fun postgresVersion() = coroutineScope.launch { }

    override fun updateUserName(userName: String) {}

    override fun updatePassword(password: String) {}

    override fun secrets() = coroutineScope.launch { }

    override fun clearSecrets() {}

    override fun detectPassword() = coroutineScope.launch { }

    override fun restoreInfo(path: String) = coroutineScope.launch { }

    override fun updateRestorePath(path: String) {}

    override fun restoreCommand(path: String) = coroutineScope.launch { }

    override fun cancelRestore() = coroutineScope.launch { }

    override fun confirmeRestore() = coroutineScope.launch { }

    override fun restoreDatabase() {}
    override val coroutineScope: CoroutineScope
        get() = CoroutineScope(EmptyCoroutineContext)
    override val showWarning: (Throwable) -> Unit = {}
    override fun showWarning(message: String?) {}
}

val ocDummy = object : OcViewModel {
    override val context: StateFlow<Context> = MutableStateFlow(Context("", listOf()))
    override val loginState: StateFlow<LoginState> =
        MutableStateFlow(LoginState.UNCHECKED)
    override val server: StateFlow<List<ClusterApi>> =
        MutableStateFlow(listOf())
    override val projects: StateFlow<List<String>> =
        MutableStateFlow(listOf())
    override val currentProject: StateFlow<String> =
        MutableStateFlow("")
    override val version: StateFlow<VersionApi> =
        MutableStateFlow(VersionApi())
    override val services: StateFlow<List<Service>> =
        MutableStateFlow(listOf())
    override val portForward: StateFlow<List<OpenPortForward>> =
        MutableStateFlow(listOf())

    override fun update() {}

    override fun context() = coroutineScope.launch { }

    override fun switchContext(newContext: FullContext) = coroutineScope.launch { }

    override fun checkLoginState() = coroutineScope.launch { }

    override fun login(token: String, server: String) = coroutineScope.launch { }

    override fun listServer() = coroutineScope.launch { }

    override fun switchProject(project: String) = coroutineScope.launch { }

    override fun portForward(svc: String, port: Int) {}

    override fun portForward(target: PortForwardTarget) {}

    override fun closePortForward(target: PortForwardTarget) {}

    override fun closeAllPortForward() {}

    override val coroutineScope: CoroutineScope
        get() = CoroutineScope(EmptyCoroutineContext)
    override val showWarning: (Throwable) -> Unit = {}
    override fun showWarning(message: String?) {}

}