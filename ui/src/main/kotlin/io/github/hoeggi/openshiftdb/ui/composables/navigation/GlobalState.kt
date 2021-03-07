package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.settings.ExportFormat
import io.github.hoeggi.openshiftdb.settings.Theme
import io.github.hoeggi.openshiftdb.settings.loadSettings
import io.github.hoeggi.openshiftdb.settings.save
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

sealed class Screen {
    object Main : Screen()
    object Detail : Screen()
}

class GlobalState : ErrorViewer {

    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)
    private var settings = loadSettings()

    private val _exportFormat: MutableStateFlow<ExportFormat> = MutableStateFlow(settings.format)
    val exportFormat = _exportFormat.asStateFlow()
    fun updateExportFormat(exportFormat: ExportFormat) {
        _exportFormat.value = exportFormat
        coroutineScope.launch {
            settings = settings.copy(format = _exportFormat.value).apply {
                save()
            }
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
    fun navigateTo(screen: Screen) {
        _screen.value = screen
    }

    private val _theme: MutableStateFlow<Theme> =
        MutableStateFlow(settings.theme)
    val theme = _theme.asStateFlow()
    fun toggleTheme() {
        _theme.value = when (_theme.value) {
            Theme.Dark -> Theme.Light
            Theme.Light -> Theme.Dark
        }
        coroutineScope.launch {
            settings = settings.copy(theme = _theme.value).apply {
                save()
            }
        }
    }

    private val _errors: MutableStateFlow<ErrorViewer.Message> = MutableStateFlow(empty())
    val errors = _errors.asStateFlow()
    override fun showError(error: ErrorViewer.Message) {
        _errors.value = error
    }

    override fun showWarning(warning: ErrorViewer.Message) {
        _errors.value = warning
    }

}