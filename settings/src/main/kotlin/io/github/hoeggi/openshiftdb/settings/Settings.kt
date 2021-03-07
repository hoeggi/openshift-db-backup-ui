package io.github.hoeggi.openshiftdb.settings

import io.github.hoeggi.settings.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

private val DefaultSettings = Settings(Theme.Dark, ExportFormat.Custom)

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
    return Json.decodeFromString(readUtf8)
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
data class Settings(val theme: Theme, val format: ExportFormat)