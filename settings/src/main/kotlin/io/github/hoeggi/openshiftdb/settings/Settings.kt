package io.github.hoeggi.openshiftdb.settings

import io.github.hoeggi.openshiftdb.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.buffer
import okio.sink
import okio.source
import java.io.File

private val settingsPath = if (System.getenv("XDG_CONFIG_HOME") != null) {
    File(System.getenv("XDG_CONFIG_HOME"))
} else {
    File(System.getProperty("user.home"), ".config")
}.let {
    File(it, "${BuildConfig.APP_NAME}${File.separator}${BuildConfig.APP_NAME}.json").apply {
        parentFile.mkdirs()
    }
}

internal val DefaultSetting = Setting(Theme.Dark, ExportFormat.Custom, LogLevel.Debug)

internal fun Setting.save() {
    val encodeToString = Json.encodeToString(this)
    settingsPath.sink().buffer().use {
        it.writeUtf8(encodeToString)
    }
}

internal fun Setting.update(
    theme: Theme? = null,
    format: ExportFormat? = null,
    logLevel: LogLevel? = null,
    fileChooser: FileChooser? = null,
): Setting {
    return copy(
        theme = theme ?: this.theme,
        format = format ?: this.format,
        logLevel = logLevel ?: this.logLevel,
        fileChooser = fileChooser ?: this.fileChooser
    ).also {
        it.save()
    }
}

internal fun loadSettings(): Setting {
    if (!settingsPath.exists()) {
        DefaultSetting.save()
    }
    val readUtf8 = settingsPath.source().buffer().readUtf8()
    return try {
        Json {
            ignoreUnknownKeys = true
        }.decodeFromString(readUtf8)
    } catch (ex: SerializationException) {
        DefaultSetting.save()
        DefaultSetting
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
    object Debug : LogLevel("Debug", { true })

    @Serializable
    object Info : LogLevel("Info", { !it.startsWith("DEBUG") && Debug.predicate(it) })

    @Serializable
    object Warn : LogLevel("Warn", { !it.startsWith("INFO") && Info.predicate(it) && Debug.predicate(it) })

    @Serializable
    object Error :
        LogLevel("Error", { !it.startsWith("WARN") && Warn.predicate(it) && Info.predicate(it) && Debug.predicate(it) })
}

@Serializable
sealed class Theme {
    @Serializable
    object Dark : Theme()

    @Serializable
    object Light : Theme()
}

@Serializable
sealed class ExportFormat(val format: String) {
    @Serializable
    object Custom : ExportFormat("custom")

    @Serializable
    object Plain : ExportFormat("plain")
}

@Serializable
sealed class FileChooser {
    @Serializable
    object Custom : FileChooser()

    @Serializable
    object System : FileChooser()
}

@Serializable
data class Setting(
    val theme: Theme = Theme.Dark,
    val format: ExportFormat = ExportFormat.Custom,
    val logLevel: LogLevel = LogLevel.Debug,
    val fileChooser: FileChooser = FileChooser.Custom,
)
