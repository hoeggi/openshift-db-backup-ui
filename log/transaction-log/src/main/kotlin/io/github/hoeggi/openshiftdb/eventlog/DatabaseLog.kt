package io.github.hoeggi.openshiftdb.eventlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseLogProvider {
    val DatabaseLog: DatabaseLog by lazy { DatabaseLogImpl }
}

interface DatabaseLog {
    suspend fun listAllDatabaseEvents(): List<DatabaseEvent>
    suspend fun insert(dte: DatabaseEvent): Long
    suspend fun listAllPortForwardEvents(): List<PortForwardEvent>
    suspend fun insert(pf: PortForwardEvent): Long
}

internal object DatabaseLogImpl : DatabaseLog {

    private val database by DatabaseInitializer()

    override suspend fun listAllPortForwardEvents() = withContext(Dispatchers.IO) {
        database.portForwardEventQueries.selectAll().executeAsList()
    }

    override suspend fun insert(pf: PortForwardEvent) = withContext(Dispatchers.IO) {
        database.portForwardEventQueries.transactionWithResult<Long> {
            database.portForwardEventQueries.insertOrReplace(
                pf.project,
                pf.service,
                pf.port,
                pf.startTime,
                pf.endTime,
                pf.type,
                pf.result,
                pf.color
            )
            database.functionsQueries.selectLastInserted().executeAsOne()
        }
    }

    override suspend fun listAllDatabaseEvents() = withContext(Dispatchers.IO) {
        database.databaseEventQueries.selectAll().executeAsList()
    }

    override suspend fun insert(dte: DatabaseEvent) = withContext(Dispatchers.IO) {
        database.databaseEventQueries.transactionWithResult<Long> {
            database.databaseEventQueries.insert(
                dte.dbname,
                dte.path,
                dte.username,
                dte.format,
                dte.startTime,
                dte.endTime,
                dte.type,
                dte.result
            )
            database.functionsQueries.selectLastInserted().executeAsOne()
        }
    }

    suspend fun events() {
//        val selectEvents = database.logEventQueries.selectEvents().asFlow().mapToList()
//        val selectExceptions: Flow<Map<Long, List<LogException>>> =
//            database.logEventQueries.selectExceptions().asFlow().mapToList()
//                .map { it.groupBy { it.event } }
//        val selectTraces: Flow<Map<Long, List<LogTrace>>> = database.logEventQueries.selectTraces().asFlow().mapToList()
//            .map { it.groupBy { it.exception } }
//
//        val combine: Flow<List<Logs>> = selectTraces.onStart { emit(mapOf()) }
//            .combine(selectExceptions.onStart { emit(mapOf()) }) { traces, exceptions -> traces to exceptions }
//            .combine(selectEvents.onStart { emit(listOf()) }) { tracesAndExceptions, events ->
//                events.map { event ->
//                    Logs(
//                        event,
//                        tracesAndExceptions.second[event.id]?.map {
//                            LogsExceptions(
//                                it,
//                                tracesAndExceptions.first[it.id]?.map { LogsTraces(it) } ?: listOf()
//                            )
//                        } ?: listOf()
//                    )
//                }
//            }
    }
}
