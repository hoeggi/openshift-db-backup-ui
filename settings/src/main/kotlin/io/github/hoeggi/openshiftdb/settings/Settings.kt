package io.github.hoeggi.openshiftdb.settings

import io.github.hoeggi.settings.BuildConfig
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import kotlin.io.path.*

@ExperimentalPathApi
private val settingsPath = if (System.getenv("XDG_CONFIG_HOME") != null) {
    File(System.getenv("XDG_CONFIG_HOME"))
} else {
    File(System.getProperty("user.home"), ".config")
}.let {
    File(it, "${BuildConfig.APP_NAME}").apply {
        mkdirs()
    }.let {
        File(it, "${BuildConfig.APP_NAME}.json")
    }
}

private val DefaultSettings = Settings(Theme.Dark, ExportFormat.Custom, LogLevel.Debug)

fun Settings.save() {
    val encodeToString = Json.encodeToString(this)
    settingsPath.sink().buffer().use {
        it.writeUtf8(encodeToString)
    }
}

fun loadSettings(): Settings {
    if (!settingsPath.exists()) {
        DefaultSettings.save()
    }
    val readUtf8 = settingsPath.source().buffer().readUtf8()
    return try {
        Json {
            ignoreUnknownKeys = true
        }.decodeFromString(readUtf8)
    } catch (ex: SerializationException) {
        DefaultSettings.save()
        DefaultSettings
    }
}

val LogLevels = listOf(
    LogLevel.Debug,
    LogLevel.Info,
    LogLevel.Warn,
    LogLevel.Error
)

@Serializable
sealed class LogLevel(val name: String, val predicate: (CharSequence) -> Boolean) {
    @Serializable
    @SerialName("Debug")
    object Debug : LogLevel("Debug", { true })

    @Serializable
    @SerialName("Info")
    object Info : LogLevel("Info", { !it.startsWith("DEBUG") && Debug.predicate(it) })

    @Serializable
    @SerialName("Warn")
    object Warn : LogLevel("Warn", { !it.startsWith("INFO") && Info.predicate(it) && Debug.predicate(it) })

    @Serializable
    @SerialName("Error")
    object Error :
        LogLevel("Error", { !it.startsWith("WARN") && Warn.predicate(it) && Info.predicate(it) && Debug.predicate(it) })
}

@Serializable
sealed class Theme {
    @Serializable
    @SerialName("dark")
    object Dark : Theme()

    @Serializable
    @SerialName("light")
    object Light : Theme()
}

@Serializable
sealed class ExportFormat(val format: String) {
    @Serializable
    @SerialName("custom")
    object Custom : ExportFormat("custom")

    @Serializable
    @SerialName("plain")
    object Plain : ExportFormat("plain")
}

@Serializable
data class Settings(
    val theme: Theme = Theme.Dark,
    val format: ExportFormat = ExportFormat.Custom,
    val logLevel: LogLevel = LogLevel.Debug,
)