package io.github.hoeggi.openshiftdb.viewmodel

import com.google.common.collect.EvictingQueue
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.api.getOrDefault
import io.github.hoeggi.openshiftdb.api.onFailure
import io.github.hoeggi.openshiftdb.api.onSuccess
import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.api.response.DatabaseRestoreMessage
import io.github.hoeggi.openshiftdb.api.response.DatabasesApi
import io.github.hoeggi.openshiftdb.api.response.RestoreCommandApi
import io.github.hoeggi.openshiftdb.api.response.SecretsApi
import io.github.hoeggi.openshiftdb.api.response.ToolsVersionApi
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

internal class PostgresViewModelImpl constructor(port: Int, errorViewer: ErrorViewer) :
    BaseViewModelImpl(port, errorViewer), PostgresViewModel {

    private val downloadQueue = EvictingQueue.create<DatabaseDownloadMessage.InProgressMessage>(150)
    private val restoreQueue = EvictingQueue.create<DatabaseRestoreMessage.InProgressMessage>(300)

    private val _dumpPath = MutableStateFlow(System.getProperty("user.home"))
    private val _password = MutableStateFlow("")
    private val _userName = MutableStateFlow("postgres")
    private val _secrets = MutableStateFlow(listOf<SecretsApi>())

    private val _databases = MutableStateFlow("")
    private val _databasesLines = MutableStateFlow(listOf<String>())
    private val _postgresVersion = MutableStateFlow("")

    private val _selectedDatabase = MutableStateFlow(-1)
    private val _version = MutableStateFlow(ToolsVersionApi())

    private val _restoreInfo = MutableStateFlow(listOf<String>())
    private val _restorePath = MutableStateFlow("")
    private val _restoreCommand = MutableStateFlow(RestoreCommandApi())

    private val _downloadProgress: MutableStateFlow<List<DatabaseDownloadMessage.InProgressMessage>> =
        MutableStateFlow(listOf())
    private val _downloadState: MutableStateFlow<DatabaseDownloadMessage> =
        MutableStateFlow(DatabaseDownloadMessage.unspecified())

    private val _restoreState: MutableStateFlow<DatabaseRestoreMessage> =
        MutableStateFlow(DatabaseRestoreMessage.unspecified())
    private val _restoreProgress: MutableStateFlow<List<DatabaseRestoreMessage.InProgressMessage>> =
        MutableStateFlow(listOf())

    override fun update() {
        version()
    }

    override val downloadState: StateFlow<DatabaseDownloadMessage> = _downloadState.asStateFlow()
    override val downloadProgress: StateFlow<List<DatabaseDownloadMessage.InProgressMessage>> =
        _downloadProgress.asStateFlow()

    override fun dumpDatabase(database: String, format: String) {
        if (database.isEmpty()) return
        coroutineScope.launch(Dispatchers.IO) {
            val dumpDatabases =
                postgresApi.dumpDatabases(userName.value, password.value, database, dumpPath.value, format)
            val eventTracker = DatabaseEventTracker(dumpPath.value, userName.value, database, format)
            dumpDatabases
                .onCompletion {
                    coroutineScope.launch(Dispatchers.IO) {
                        val newTransaction = eventsApi.newEvent(eventTracker.event())
                        newTransaction.onSuccess {
                            logger.debug("tracked new transaction $it")
                        }.onFailure {
                            logger.error("error sending transaction", it)
                        }
                    }
                }
                .collect {
                    eventTracker.trackMessage(it)
                    when (it) {
                        is DatabaseDownloadMessage.InProgressMessage -> {
                            downloadQueue.add(it)
                            _downloadProgress.value = downloadQueue.toList()
                            _downloadState.value = it
                        }
                        else -> {
                            downloadQueue.clear()
                            _downloadState.value = it
                        }
                    }
                    when (it) {
                        is DatabaseDownloadMessage.FinishMessage -> {
                            eventTracker.path = it.message
                            cancel(it.message)
                        }
                        is DatabaseDownloadMessage.ErrorMessage -> cancel(it.message)
                    }
                }
        }
    }

    override val version = _version.asStateFlow()
    private fun version() = coroutineScope.launch {
        _version.value = postgresApi.toolsVersion().getOrDefault(ToolsVersionApi())
    }

    override val dumpPath = _dumpPath.asStateFlow()
    override fun dumpPath(path: String) {
        _dumpPath.value = path
    }

    override val selectedDatabase = _selectedDatabase.asStateFlow()
    override fun updateSelectedDatabase(index: Int) {
        _selectedDatabase.value = index
    }

    override val databasesLines = _databasesLines.asStateFlow()
    override fun listLines() = coroutineScope.launch {
        val databases = postgresApi.databases(userName.value, password.value, PostgresApi.DatabaseViewFormat.List)
        databases.onSuccess {
            _databasesLines.value = when (it) {
                is DatabasesApi.List -> it.databases.map { it }
                else -> listOf("")
            }
            selectDefaultDatabase()
        }.onFailure(showWarning)
    }

    override val databases = _databases.asStateFlow()
    override fun clearDatabaseText() {
        _databases.value = ""
    }

    override fun listPretty() = coroutineScope.launch {
        val databases = postgresApi.databases(userName.value, password.value, PostgresApi.DatabaseViewFormat.Table)
        databases.onSuccess {
            _databases.value = when (it) {
                is DatabasesApi.Tabel -> it.databases
                else -> ""
            }
        }.onFailure(showWarning)
    }

    override val postgresVersion = _postgresVersion.asStateFlow()
    override fun postgresVersion() = coroutineScope.launch {
        val databaseVersion = postgresApi.databaseVersion(userName.value, password.value)
        databaseVersion.onSuccess {
            _postgresVersion.value = it.database
        }.onFailure(showWarning)
    }

    override val userName = _userName.asStateFlow()
    override fun updateUserName(userName: String) {
        _userName.value = userName
    }

    override val password = _password.asStateFlow()
    override fun updatePassword(password: String) {
        _password.value = password
    }

    override val secrets = _secrets.asStateFlow()
    override fun secrets() = coroutineScope.launch {
        val result = ocApi.secrets()
        result.onSuccess {
            _secrets.value = it
        }.onFailure(showWarning)
    }

    override fun clearSecrets() {
        _secrets.value = listOf()
    }

    override fun detectPassword() = coroutineScope.launch {
        val result = ocApi.password(userName.value)
        result.onSuccess {
            _password.value = it
        }.onFailure(showWarning)
    }

    private fun selectDefaultDatabase() = coroutineScope.launch {
        val default = postgresApi.defaultDatabases(userName.value, password.value)
        default.onSuccess { db ->
            val defaultDb = databasesLines.value.indexOfFirst {
                it == db.database
            }
            _selectedDatabase.value = defaultDb
        }.onFailure(showWarning)
    }

    override val restoreInfo = _restoreInfo.asStateFlow()
    override fun restoreInfo(path: String) = coroutineScope.launch {
        val databases = postgresApi.restoreInfo(path)
        databases.onSuccess {
            _restoreInfo.value = it.info
        }.onFailure(showWarning)
    }

    override val restorePath = _restorePath.asStateFlow()
    override fun updateRestorePath(path: String) {
        _restorePath.value = path
        restoreInfo(path)
        restoreCommand(path)
    }

    override val restoreCommand = _restoreCommand.asStateFlow()
    override fun restoreCommand(path: String) = coroutineScope.launch {
        val restoreCommand = postgresApi.restoreCommand(userName.value, password.value, path)
        restoreCommand.onSuccess {
            _restoreCommand.value = it
        }.onFailure(showWarning)
    }

    private var confirmationChannel = Channel<Boolean>()
    override fun cancelRestore() = coroutineScope.launch {
        if (!confirmationChannel.isClosedForSend) confirmationChannel.send(false)
    }

    override fun confirmeRestore() = coroutineScope.launch {
        if (!confirmationChannel.isClosedForSend) confirmationChannel.send(true)
    }

    override val restoreState: StateFlow<DatabaseRestoreMessage> = _restoreState.asStateFlow()
    override val restoreProgress: StateFlow<List<DatabaseRestoreMessage.InProgressMessage>> =
        _restoreProgress.asStateFlow()

    override fun restoreDatabase() {
        coroutineScope.launch(Dispatchers.IO) {
            val dumpDatabases =
                postgresApi.restoreDatabase(
                    userName.value,
                    password.value,
                    restorePath.value,
                    restoreCommand.value.database,
                    restoreCommand.value.existing,
                    confirmationChannel
                )
            val eventTracker =
                DatabaseEventTracker(restorePath.value, userName.value, restoreCommand.value.database, "custom")
            dumpDatabases
                .onCompletion {
                    coroutineScope.launch(Dispatchers.IO) {
                        val newTransaction = eventsApi.newEvent(eventTracker.event())
                        newTransaction.onSuccess {
                            logger.debug("tracked new transaction $it")
                        }.onFailure {
                            logger.error("error sending transaction", it)
                        }
                    }
                }.collect {
                    eventTracker.trackMessage(it)
                    when (it) {
                        is DatabaseRestoreMessage.InProgressMessage -> {
                            restoreQueue.add(it)
                            _restoreProgress.value = restoreQueue.toList()
                            _restoreState.value = it
                        }
                        else -> {
                            restoreQueue.clear()
                            _restoreState.value = it
                        }
                    }
                    when (it) {
                        is DatabaseRestoreMessage.FinishMessage,
                        is DatabaseRestoreMessage.ErrorMessage,
                        -> cancel("finished, $it")
                    }
                }
        }
    }
}
