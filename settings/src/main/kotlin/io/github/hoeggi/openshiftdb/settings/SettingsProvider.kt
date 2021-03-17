package io.github.hoeggi.openshiftdb.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

object SettingsProvider {
    fun instance(): Settings = SettingsHolder

    val settingsDummy = object : Settings {
        override val theme: StateFlow<Theme> = MutableStateFlow(Theme.Dark)

        override fun themeDark() {}

        override fun themeLight() {}

        override val logLevel: StateFlow<LogLevel> = MutableStateFlow(LogLevel.Debug)

        override fun updateLogLevel(logLevel: LogLevel) {}

        override val exportFormat: StateFlow<ExportFormat> = MutableStateFlow(ExportFormat.Custom)

        override fun updateExportFormat(exportFormat: ExportFormat) {}
    }
}

interface Settings {
    val theme: StateFlow<Theme>
    fun themeDark()
    fun themeLight()


    val logLevel: StateFlow<LogLevel>
    fun updateLogLevel(logLevel: LogLevel)

    val exportFormat: StateFlow<ExportFormat>
    fun updateExportFormat(exportFormat: ExportFormat)
}

private object SettingsHolder : Settings {

    private var settings = loadSettings()
    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)


    private val _theme: MutableStateFlow<Theme> =
        MutableStateFlow(settings.theme)
    override val theme = _theme.asStateFlow()
    override fun themeDark() {
        _theme.value = Theme.Dark
        coroutineScope.launch {
            settings = settings.update(theme = _theme.value)
        }
    }

    override fun themeLight() {
        _theme.value = Theme.Light
        coroutineScope.launch {
            settings = settings.update(theme = _theme.value)
        }
    }


    private val _logLevel: MutableStateFlow<LogLevel> = MutableStateFlow(settings.logLevel)
    override val logLevel = _logLevel.asStateFlow()
    override fun updateLogLevel(logLevel: LogLevel) {
        _logLevel.value = logLevel
        coroutineScope.launch {
            settings = settings.update(logLevel = _logLevel.value)
        }
    }

    private val _exportFormat: MutableStateFlow<ExportFormat> = MutableStateFlow(settings.format)
    override val exportFormat = _exportFormat.asStateFlow()
    override fun updateExportFormat(exportFormat: ExportFormat) {
        _exportFormat.value = exportFormat
        coroutineScope.launch {
            settings = settings.update(format = _exportFormat.value)
        }
    }
}