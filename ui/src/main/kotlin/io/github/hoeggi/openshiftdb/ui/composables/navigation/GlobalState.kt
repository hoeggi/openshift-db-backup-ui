package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.settings.*
import io.github.hoeggi.openshiftdb.ui.theme.CustomOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

sealed class Screen {
    object Main : Screen()
    object Detail : Screen()
    object Restore : Screen()
}

class GlobalState : ErrorViewer {

//    val test = CoroutineScope()
    private val _refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow()
    val refreshTrigger = _refreshTrigger.asSharedFlow()
    fun refresh(coroutineScope: CoroutineScope) = coroutineScope.launch {
        _refreshTrigger.emit(Unit)
    }

    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)
    private var settings = loadSettings()

    private val _showDrawer: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showDrawer = _showDrawer.asStateFlow()
    fun toggleDrawer() {
        _showDrawer.value = !_showDrawer.value
    }

    fun hideDrawer() {
        _showDrawer.value = false
    }

    private val _logLevel: MutableStateFlow<LogLevel> = MutableStateFlow(settings.logLevel)
    val logLevel = _logLevel.asStateFlow()
    fun updateLogLevel(logLevel: LogLevel) {
        _logLevel.value = logLevel
        coroutineScope.launch {
            settings = settings.update(logLevel = _logLevel.value)
        }
    }

    private val _exportFormat: MutableStateFlow<ExportFormat> = MutableStateFlow(settings.format)
    val exportFormat = _exportFormat.asStateFlow()
    fun updateExportFormat(exportFormat: ExportFormat) {
        _exportFormat.value = exportFormat
        coroutineScope.launch {
            settings = settings.update(format = _exportFormat.value)
        }
    }

    private val _syslog = MutableStateFlow(
        listOf(AnnotatedString("", SpanStyle()))
    )
    val syslog = _syslog.asStateFlow()
    fun updateSyslog(log: List<AnnotatedString>) {
        _syslog.value = log
    }

    private val _screen: MutableStateFlow<Screen> =
        MutableStateFlow(Screen.Main)
    val screen = _screen.asStateFlow()

    fun main() = navigateTo(Screen.Main)
    fun detail() = navigateTo(Screen.Detail)
    fun restore() = navigateTo(Screen.Restore)
    fun navigateTo(screen: Screen) {
        _screen.value = screen
    }

    private val _theme: MutableStateFlow<Theme> =
        MutableStateFlow(settings.theme)
    val theme = _theme.asStateFlow()
    fun themeDark() {
        _theme.value = Theme.Dark
        coroutineScope.launch {
            settings = settings.update(theme = _theme.value)
        }
    }

    fun themeLight() {
        _theme.value = Theme.Light
        coroutineScope.launch {
            settings = settings.update(theme = _theme.value)
        }
    }

    fun toggleTheme() {
        _theme.value = when (_theme.value) {
            Theme.Dark -> Theme.Light
            Theme.Light -> Theme.Dark
        }
        coroutineScope.launch {
            settings = settings.update(theme = _theme.value)
        }
    }

    private val _errors: MutableStateFlow<ErrorViewer.Message> = MutableStateFlow(empty())
    val errors = _errors.asStateFlow()

    fun resetOverlay() {
        _errors.value = empty()
    }

    fun showOverlay(overlay: CustomOverlay) {
        _errors.value = overlay
    }

    override fun showError(error: ErrorViewer.Message) {
        _errors.value = error
    }

    override fun showWarning(warning: ErrorViewer.Message) {
        _errors.value = warning
    }

    private fun Settings.update(
        theme: Theme? = null,
        format: ExportFormat? = null,
        logLevel: LogLevel? = null,
    ): Settings {
        return copy(
            theme = theme ?: this.theme,
            format = format ?: this.format,
            logLevel = logLevel ?: this.logLevel
        ).also {
            it.save()
        }
    }
}