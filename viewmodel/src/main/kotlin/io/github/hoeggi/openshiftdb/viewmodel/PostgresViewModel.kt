package io.github.hoeggi.openshiftdb.viewmodel

import com.google.common.collect.EvictingQueue
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.api.getOrDefault
import io.github.hoeggi.openshiftdb.api.onFailure
import io.github.hoeggi.openshiftdb.api.onSuccess
import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.api.response.DatabasesApi
import io.github.hoeggi.openshiftdb.api.response.SecretsApi
import io.github.hoeggi.openshiftdb.api.response.ToolsVersionApi
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PostgresViewModel(port: Int, coroutineScope: CoroutineScope, errorViewer: ErrorViewer) :
    BaseViewModel(port, coroutineScope, errorViewer) {
    private val downloadQueue = EvictingQueue.create<DatabaseDownloadMessage.InProgressMessage>(150)

    private val _dumpPath = MutableStateFlow(System.getProperty("user.home"))
    private val _password = MutableStateFlow("")
    private val _userName = MutableStateFlow("postgres")
    private val _secrets = MutableStateFlow(listOf<SecretsApi>())

    private val _databases = MutableStateFlow("")
    private val _databasesLines = MutableStateFlow(listOf<String>())
    private val _postgresVersion = MutableStateFlow("")

    private val _selectedDatabase = MutableStateFlow(-1)
    private val _version = MutableStateFlow(ToolsVersionApi())


    private val _downloadProgress: MutableStateFlow<List<DatabaseDownloadMessage.InProgressMessage>> =
        MutableStateFlow(listOf())
    private val _downloadState: MutableStateFlow<DatabaseDownloadMessage> =
        MutableStateFlow(DatabaseDownloadMessage.unspecified())

    fun update() {
        version()
    }

    val downloadState: StateFlow<DatabaseDownloadMessage> = _downloadState.asStateFlow()
    val downloadProgress: StateFlow<List<DatabaseDownloadMessage.InProgressMessage>> = _downloadProgress.asStateFlow()
    fun dumpDatabase(database: String, format: String) {
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

    val version = _version.asStateFlow()
    private fun version() = coroutineScope.launch {
        _version.value = postgresApi.toolsVersion().getOrDefault(ToolsVersionApi())
    }

    val dumpPath = _dumpPath.asStateFlow()
    fun dumpPath(path: String) {
        _dumpPath.value = path
    }

    val selectedDatabase = _selectedDatabase.asStateFlow()
    fun updateSelectedDatabase(index: Int) {
        _selectedDatabase.value = index
    }

    val databasesLines = _databasesLines.asStateFlow()
    fun listLines() = coroutineScope.launch {
        val databases = postgresApi.databases(userName.value, password.value, PostgresApi.DatabaseViewFormat.List)
        databases.onSuccess {
            _databasesLines.value = when (it) {
                is DatabasesApi.List -> it.databases.map { it }
                else -> listOf("")
            }
            selectDefaultDatabase()
        }.onFailure(showWarning)
    }

    val databases = _databases.asStateFlow()
    fun listPretty() = coroutineScope.launch {
        val databases = postgresApi.databases(userName.value, password.value, PostgresApi.DatabaseViewFormat.Table)
        databases.onSuccess {
            _databases.value = when (it) {
                is DatabasesApi.Tabel -> it.databases
                else -> ""
            }
        }.onFailure(showWarning)
    }

    val postgresVersion = _postgresVersion.asStateFlow()
    fun postgresVersion() = coroutineScope.launch {
        val databaseVersion = postgresApi.databaseVersion(userName.value, password.value)
        databaseVersion.onSuccess {
            _postgresVersion.value = it.database
        }.onFailure(showWarning)
    }

    val userName = _userName.asStateFlow()
    fun updateUserName(userName: String) {
        _userName.value = userName
    }

    val password = _password.asStateFlow()
    fun updatePassword(password: String) {
        _password.value = password
    }

    val secrets = _secrets.asStateFlow()
    fun secrets() = coroutineScope.launch {
        val result = ocApi.secrets()
        result.onSuccess {
            _secrets.value = it
        }.onFailure(showWarning)
    }

    fun clearSecrets() {
        _secrets.value = listOf()
    }

    fun detectPassword() = coroutineScope.launch {
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
}