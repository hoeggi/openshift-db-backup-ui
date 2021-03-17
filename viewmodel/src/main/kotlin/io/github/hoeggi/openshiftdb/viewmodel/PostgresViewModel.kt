package io.github.hoeggi.openshiftdb.viewmodel

import com.google.common.collect.EvictingQueue
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.api.getOrDefault
import io.github.hoeggi.openshiftdb.api.onFailure
import io.github.hoeggi.openshiftdb.api.onSuccess
import io.github.hoeggi.openshiftdb.api.response.*
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

interface PostgresViewModel: ViewModel {
    val downloadState: StateFlow<DatabaseDownloadMessage>
    val downloadProgress: StateFlow<List<DatabaseDownloadMessage.InProgressMessage>>
    val version: StateFlow<ToolsVersionApi>
    val dumpPath: StateFlow<String>
    val selectedDatabase: StateFlow<Int>
    val databasesLines: StateFlow<List<String>>
    val databases: StateFlow<String>
    val postgresVersion: StateFlow<String>
    val userName: StateFlow<String>
    val password: StateFlow<String>
    val secrets: StateFlow<List<SecretsApi>>
    val restoreInfo: StateFlow<List<String>>
    val restorePath: StateFlow<String>
    val restoreCommand: StateFlow<RestoreCommandApi>
    val restoreState: StateFlow<DatabaseRestoreMessage>
    val restoreProgress: StateFlow<List<DatabaseRestoreMessage.InProgressMessage>>
    fun update()
    fun dumpDatabase(database: String, format: String)
    fun dumpPath(path: String)
    fun updateSelectedDatabase(index: Int)
    fun listLines(): Job
    fun clearDatabaseText()
    fun listPretty(): Job
    fun postgresVersion(): Job
    fun updateUserName(userName: String)
    fun updatePassword(password: String)
    fun secrets(): Job
    fun clearSecrets()
    fun detectPassword(): Job
    fun restoreInfo(path: String): Job
    fun updateRestorePath(path: String)
    fun restoreCommand(path: String): Job
    fun cancelRestore(): Job
    fun confirmeRestore(): Job
    fun restoreDatabase()
}

internal class PostgresViewModelImpl internal constructor(port: Int, coroutineScope: CoroutineScope, errorViewer: ErrorViewer) :
    BaseViewModel(port, coroutineScope, errorViewer), PostgresViewModel {

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
    override val downloadProgress: StateFlow<List<DatabaseDownloadMessage.InProgressMessage>> = _downloadProgress.asStateFlow()
    override fun dumpDatabase(database: String, format: String) {
        if (database.isEmpty()) return
        coroutineScope.launch(Dispatchers.IO) {
            val dumpDatabases =
                postgresApi.dumpDatabases(userName.value, password.value, database, dumpPath.value, format)
            dumpDatabases.collect {
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
    override val restoreProgress: StateFlow<List<DatabaseRestoreMessage.InProgressMessage>> = _restoreProgress.asStateFlow()
    override fun restoreDatabase() {
        coroutineScope.launch(Dispatchers.IO) {
            val dumpDatabases =
                postgresApi.restoreDatabase(userName.value,
                    password.value,
                    restorePath.value,
                    restoreCommand.value.database,
                    restoreCommand.value.existing,
                    confirmationChannel)
            dumpDatabases.collect {
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
            }
        }
    }
}