package io.github.hoeggi.openshiftdb.eventlog

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.github.hoeggi.openshiftdb.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val LocalDateTimeAdapter = object : ColumnAdapter<LocalDateTime, String> {
    override fun decode(databaseValue: String) = LocalDateTime.parse(databaseValue, DateTimeFormatter.ISO_DATE_TIME)
    override fun encode(value: LocalDateTime) = value.format(DateTimeFormatter.ISO_DATE_TIME)
}

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
    private val dbFilePath = if (System.getenv("XDG_CONFIG_HOME") != null) {
        File(System.getenv("XDG_CONFIG_HOME"))
    } else {
        File(System.getProperty("user.home"), ".config")
    }.let {
        File(it, "${BuildConfig.APP_NAME}${File.separator}${BuildConfig.APP_NAME}.sqlite").apply {
            parentFile.mkdirs()
        }
    }
    private val database by lazy {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${dbFilePath.absolutePath}")
        migrateIfNeeded(driver)
        OpenshiftDbGui(
            driver = driver,
            DatabaseEventAdapter = DatabaseEvent.Adapter(
                resultAdapter = EnumColumnAdapter(),
                typeAdapter = EnumColumnAdapter(),
                startTimeAdapter = LocalDateTimeAdapter,
                endTimeAdapter = LocalDateTimeAdapter
            ),
            PortForwardEventAdapter = PortForwardEvent.Adapter(
                resultAdapter = EnumColumnAdapter(),
                typeAdapter = EnumColumnAdapter(),
                startTimeAdapter = LocalDateTimeAdapter,
                endTimeAdapter = LocalDateTimeAdapter
            )
        )
    }

    override suspend fun listAllPortForwardEvents()= withContext(Dispatchers.IO) {
        database.portForwardEventQueries.listAll().executeAsList()
    }

    override suspend fun insert(pf: PortForwardEvent) = withContext(Dispatchers.IO) {
        database.portForwardEventQueries.transactionWithResult<Long> {
            database.portForwardEventQueries.insert(
                pf.project,
                pf.service,
                pf.port,
                pf.startTime,
                pf.endTime,
                pf.type,
                pf.result
            )
            database.functionsQueries.selectLastInserted().executeAsOne()
        }
    }

    override suspend fun listAllDatabaseEvents() = withContext(Dispatchers.IO) {
        database.databaseEventQueries.listAll().executeAsList()
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

    private const val versionPragma = "user_version"

    private fun migrateIfNeeded(driver: SqlDriver) {
        val oldVersion =
            driver.executeQuery(null, "PRAGMA $versionPragma", 0).use { cursor ->
                if (cursor.next()) {
                    cursor.getLong(0)?.toInt()
                } else {
                    null
                }
            } ?: 0

        val newVersion = OpenshiftDbGui.Schema.version
        if (oldVersion == 0) {
            println("Creating DB version $newVersion!")
            OpenshiftDbGui.Schema.create(driver)
            driver.execute(null, "PRAGMA $versionPragma=$newVersion", 0)
        } else if (oldVersion < newVersion) {
            println("Migrating DB from version $oldVersion to $newVersion!")
            OpenshiftDbGui.Schema.migrate(driver, oldVersion, newVersion)
            driver.execute(null, "PRAGMA $versionPragma=$newVersion", 0)
        }
    }
}