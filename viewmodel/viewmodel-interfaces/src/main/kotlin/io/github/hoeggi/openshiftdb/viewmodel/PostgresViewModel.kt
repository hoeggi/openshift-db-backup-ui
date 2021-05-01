package io.github.hoeggi.openshiftdb.viewmodel

import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.api.response.DatabaseRestoreMessage
import io.github.hoeggi.openshiftdb.api.response.RestoreCommandApi
import io.github.hoeggi.openshiftdb.api.response.SecretsApi
import io.github.hoeggi.openshiftdb.api.response.ToolsVersionApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

interface PostgresViewModel : BaseViewModel {
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
