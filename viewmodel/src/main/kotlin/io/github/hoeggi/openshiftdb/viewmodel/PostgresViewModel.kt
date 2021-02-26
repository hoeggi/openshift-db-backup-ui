package io.github.hoeggi.openshiftdb.viewmodel

import com.google.common.collect.EvictingQueue
import io.github.hoeggi.openshiftdb.api.Api
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.api.response.DatabasesApi
import io.github.hoeggi.openshiftdb.api.response.ToolsVersionApi
import kotlinx.coroutines.flow.*

//interface UserName {
//    val text: String
//}
//
//interface Password {
//    val text: String
//}
//
//private class UserNameImpl(override val text: String = "") : UserName
//private class PasswordImpl(override val text: String = "") : Password

class PostgresViewModel constructor(
    port: Int, private val api: Api = Api(port)
) {

    private val downloadQueue = EvictingQueue.create<DatabaseDownloadMessage>(150)

    private val _dumpPath = MutableStateFlow("")
    private val _password = MutableStateFlow("postgres")
    private val _userName = MutableStateFlow("")

    private val _databases = MutableStateFlow("")
    private val _databasesLines = MutableStateFlow(listOf<String>())
    private val _postgresVersion = MutableStateFlow("")

    private val _selectedDatabase = MutableStateFlow(-1)
    private val _version = MutableStateFlow(ToolsVersionApi())


    private val _downloadProgress: MutableStateFlow<List<DatabaseDownloadMessage>> =
        MutableStateFlow(listOf())
    private val _downloadState: MutableStateFlow<DatabaseDownloadMessage> =
        MutableStateFlow(DatabaseDownloadMessage.unspecified())

    suspend fun update() {
        version()
    }

    val downloadState = _downloadState.asStateFlow()
    val downloadProgress = _downloadProgress.asStateFlow()
    suspend fun dumpDatabase(database: String) {
        if (database.isEmpty()) return
        val dumpDatabases = api.dumpDatabases(userName.value, password.value, database, dumpPath.value)
        dumpDatabases.collect {
            when (it) {
                is DatabaseDownloadMessage.InProgressMessage -> {
                    downloadQueue.add(it)
                    _downloadProgress.value = downloadQueue.toList()
                }
                else -> {
                    downloadQueue.clear()
                    _downloadState.value = it
                }
            }

        }
    }

    val version = _version.asStateFlow()
    suspend fun version() {
        _version.value = api.toolsVersion().getOrDefault(ToolsVersionApi())
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
    suspend fun listLines() {
        val databases = api.databases(userName.value, password.value, PostgresApi.DatabaseViewFormat.List)
        databases.onSuccess {
            _databasesLines.value = when (it) {
                is DatabasesApi.List -> it.databases.map { it }
                else -> listOf("")
            }
            selectDefaultDatabase()
        }
    }

    val databases = _databases.asStateFlow()
    suspend fun listPretty() {
        val databases = api.databases(userName.value, password.value, PostgresApi.DatabaseViewFormat.Table)
        databases.onSuccess {
            _databases.value = when (it) {
                is DatabasesApi.Text -> it.databases
                else -> ""
            }
        }
    }

    val postgresVersion = _postgresVersion.asStateFlow()
    suspend fun postgresVersion() {
        val databaseVersion = api.databaseVersion(userName.value, password.value)
        databaseVersion.onSuccess {
            _postgresVersion.value = it.database
        }
    }

    val userName = _userName.asStateFlow()
    fun updateUserName(userName: String) {
        _userName.value = userName
    }

    val password = _password.asStateFlow()
    fun updatePassword(password: String) {
        _password.value = password
    }


    suspend fun detectPassword() {
        val result = api.password(userName.value)
        result.onSuccess {
            _password.value = it
        }
    }

    private suspend fun selectDefaultDatabase() {
        //        postgres.defaultDB(password.value.text).replace("\n", "")
        val default = api.defaultDatabases(userName.value, password.value)
        default.onSuccess { db ->
            val defaultDb = databasesLines.value.indexOfFirst {
                it == db.database
            }
            _selectedDatabase.value = defaultDb
        }
    }
}