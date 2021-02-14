package io.github.hoeggi.openshiftdb.ntcdesktop.ui.viewmodel

import com.google.common.collect.EvictingQueue
import io.github.hoeggi.openshiftdb.ntcdesktop.process.DatabaseDownloader
import io.github.hoeggi.openshiftdb.ntcdesktop.process.OC
import io.github.hoeggi.openshiftdb.ntcdesktop.process.Postgres
import io.github.hoeggi.openshiftdb.ntcdesktop.process.findPassword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    private val _password = MutableStateFlow("")
    private val _selectedDatabase = MutableStateFlow(-1)
    private val _postgresVersion = MutableStateFlow("")
    private val _pgdupmVersion = MutableStateFlow("")
    private val _dumpPath = MutableStateFlow("")
//    private val _dump: MutableStateFlow<Postgres.PostgresResult.Dump?> = MutableStateFlow(null)

    private val _downloadState: MutableStateFlow<Postgres.PostgresResult.Download> =
        MutableStateFlow(Postgres.PostgresResult.Download.Unspecified)

    //5xhdr;T,D+Fs6NE})mJ:{Fsw

    init {
        psqlVersion()
        pgdupmVersion()
    }

    val downloadState = _downloadState.asStateFlow()
    fun dumpDatabase(database: String) = coroutineScope.launch {
        if (database.isEmpty()) return@launch
        _downloadState.value = Postgres.PostgresResult.Download.Started
        postgresDownloader.download(
            dump = postgres.dumpDatabase(database, dumpPath.value, password.value),
            onNewLine = {
                downloadQueue.add(it)
                _downloadState.value = Postgres.PostgresResult.Download.InProgres(downloadQueue.toList())
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

    val dumpPath = _dumpPath.asStateFlow()
    fun dumpPath(path: String) = coroutineScope.launch {
        _dumpPath.value = path
    }

    val pgdupmVersion = _pgdupmVersion.asStateFlow()
    fun pgdupmVersion() = coroutineScope.launch {
        _pgdupmVersion.value = postgres.pq_dumpVersion()
    }

    val postgresVersion = _postgresVersion.asStateFlow()
    fun postgresVersion() = coroutineScope.launch {
        _postgresVersion.value = postgres.postgresVersion(password.value)
    }

    val selectedDatabase = _selectedDatabase.asStateFlow()
    fun updateSelectedDatabase(index: Int) = coroutineScope.launch {
        println(index)
        _selectedDatabase.value = index
    }

    val password = _password.asStateFlow()
    fun updatePassword(password: String) {
        _password.value = password
    }

    val databasesLines = _databasesLines.asStateFlow()
    fun listLines() = coroutineScope.launch {
        _databasesLines.value = postgres.listLines(password.value)
    }

    val databases = _databases.asStateFlow()
    fun listPretty() = coroutineScope.launch {
        _databases.value = postgres.listPretty(password.value)
    }

    val psqlVersion = _psqlVersion.asStateFlow()
    fun psqlVersion() = coroutineScope.launch {
        _psqlVersion.value = postgres.psqlVersion()
    }

    fun detectPassword() = coroutineScope.launch {
        val secrets = oc.secrets()
        _password.value = findPassword(secrets.json) ?: ""
    }
}