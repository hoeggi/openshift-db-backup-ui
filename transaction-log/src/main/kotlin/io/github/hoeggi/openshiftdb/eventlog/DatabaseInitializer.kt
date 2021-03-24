package io.github.hoeggi.openshiftdb.eventlog

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.github.hoeggi.openshiftdb.BuildConfig
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val LocalDateTimeAdapter = object : ColumnAdapter<LocalDateTime, String> {
    override fun decode(databaseValue: String) = LocalDateTime.parse(databaseValue, DateTimeFormatter.ISO_DATE_TIME)
    override fun encode(value: LocalDateTime) = value.format(DateTimeFormatter.ISO_DATE_TIME)
}

internal object DatabaseInitializer {

    operator fun invoke() = lazy {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${dbFilePath.absolutePath}")
        migrateIfNeeded(driver)
        val database = OpenshiftDbGui(
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
        database.portForwardEventQueries.transaction {
            database.portForwardEventQueries.closeAllOpen(LocalDateTime.now())
        }
        database
    }

    private val dbFilePath = if (System.getenv("XDG_CONFIG_HOME") != null) {
        File(System.getenv("XDG_CONFIG_HOME"))
    } else {
        File(System.getProperty("user.home"), ".config")
    }.let {
        File(it, "${BuildConfig.APP_NAME}${File.separator}${BuildConfig.APP_NAME}.sqlite").apply {
            parentFile.mkdirs()
        }
    }

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

    private const val versionPragma = "user_version"
}
