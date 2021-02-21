package io.github.hoeggi.openshiftdb.ui.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import com.google.common.collect.EvictingQueue
import io.github.hoeggi.openshiftdb.process.DatabaseDownloader
import io.github.hoeggi.openshiftdb.process.OC
import io.github.hoeggi.openshiftdb.process.Postgres
import io.github.hoeggi.openshiftdb.process.findPassword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostgresViewModel(
    private val postgres: Postgres,
    private val oc: OC,
    private val coroutineScope: CoroutineScope,
    private val postgresDownloader: DatabaseDownloader = DatabaseDownloader()
) {

    private val downloadQueue = EvictingQueue.create<String>(150)

    private val _databases = MutableStateFlow("")
    private val _databasesLines = MutableStateFlow(listOf<String>())
    private val _psqlVersion = MutableStateFlow("")
    private val _password = MutableStateFlow(TextFieldValue(""))
    private val _selectedDatabase = MutableStateFlow(-1)
    private val _postgresVersion = MutableStateFlow("")
    private val _pgdupmVersion = MutableStateFlow("")
    private val _dumpPath = MutableStateFlow("")
    private val _userName = MutableStateFlow(TextFieldValue("postgres"))
    private val _downloadProgress: MutableStateFlow<Postgres.PostgresResult.Download> =
        MutableStateFlow(Postgres.PostgresResult.Download.Unspecified)
    private val _downloadState: MutableStateFlow<Postgres.PostgresResult.Download> =
        MutableStateFlow(Postgres.PostgresResult.Download.Unspecified)

    init {
        psqlVersion()
        pgdupmVersion()
    }

    val downloadState = _downloadState.asStateFlow()
    val downloadProgress = _downloadProgress.asStateFlow()
    fun dumpDatabase(database: String) = coroutineScope.launch {
        if (database.isEmpty()) return@launch
        _downloadState.value = Postgres.PostgresResult.Download.Started
        postgresDownloader.download(
            dump = postgres.dumpDatabase(database, dumpPath.value, password.value.text),
            onNewLine = {
                downloadQueue.add(it)
                val progress = Postgres.PostgresResult.Download.InProgres(downloadQueue.toList())
                _downloadState.value = progress
                _downloadProgress.value = progress
            },
            onError = { code, ex ->
                downloadQueue.clear()
                _downloadState.value = Postgres.PostgresResult.Download.Error(code, ex)
            },
            onSuccess = {
                downloadQueue.clear()
                _downloadState.value = Postgres.PostgresResult.Download.Success(it)
            }
        )
    }

    val userName = _userName.asStateFlow()
    fun updateUserName(userName: TextFieldValue) = coroutineScope.launch {
        _userName.value = userName
    }

    val dumpPath = _dumpPath.asStateFlow()
    fun dumpPath(path: String) = coroutineScope.launch {
        _dumpPath.value = path
    }

    val pgdupmVersion = _pgdupmVersion.asStateFlow()
    fun pgdupmVersion() = coroutineScope.launch {
        _pgdupmVersion.value = postgres.pqDumpVersion()
    }

    val postgresVersion = _postgresVersion.asStateFlow()
    fun postgresVersion() = coroutineScope.launch {
        _postgresVersion.value = postgres.postgresVersion(password.value.text)
    }

    val selectedDatabase = _selectedDatabase.asStateFlow()
    fun updateSelectedDatabase(index: Int) = coroutineScope.launch {
        _selectedDatabase.value = index
    }

    val password = _password.asStateFlow()
    fun updatePassword(password: TextFieldValue) {
        _password.value = password
    }

    val databasesLines = _databasesLines.asStateFlow()
    fun listLines() = coroutineScope.launch {
        _databasesLines.value = postgres.listLines(password.value.text)
        selectDefaultDatabase()
    }

    val databases = _databases.asStateFlow()
    fun listPretty() = coroutineScope.launch {
        _databases.value = postgres.listPretty(password.value.text)
    }

    val psqlVersion = _psqlVersion.asStateFlow()
    fun psqlVersion() = coroutineScope.launch {
        _psqlVersion.value = postgres.psqlVersion()
    }

    fun detectPassword() = coroutineScope.launch {
        val secrets = oc.secrets()
        _password.value = TextFieldValue(findPassword(secrets.json, userName.value.text) ?: "")
    }

    private fun selectDefaultDatabase() = coroutineScope.launch {
        val default = postgres.defaultDB(password.value.text).replace("\n", "")
        val defaultDb = databasesLines.value.indexOfFirst {
            it == default
        }
        _selectedDatabase.value = defaultDb
    }
}