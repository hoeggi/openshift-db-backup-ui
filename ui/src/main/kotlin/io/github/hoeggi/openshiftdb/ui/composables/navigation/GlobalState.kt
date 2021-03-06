package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class Screen {
    object Main : Screen()
    object Detail : Screen()
}

sealed class Theme {
    object Dark : Theme()
    object Light : Theme()
}

sealed class ExportFormat(val format: String) {
    object Custom : ExportFormat("custom")
    object Plain : ExportFormat("plain")
}

class GlobalState : ErrorViewer {
    private val _exportFormat: MutableStateFlow<ExportFormat> = MutableStateFlow(ExportFormat.Custom)
    val exportFormat = _exportFormat.asStateFlow()
    fun updateExportFormat(exportFormat: ExportFormat) {
        _exportFormat.value = exportFormat
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
        MutableStateFlow(Theme.Dark)
    val theme = _theme.asStateFlow()

    fun toggleTheme() {
        _theme.value = when (_theme.value) {
            Theme.Dark -> Theme.Light
            Theme.Light -> Theme.Dark
        }
    }

    private val _settings: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val settings = _settings.asStateFlow()

    fun toggleSettings() {
        _settings.value = !_settings.value
    }

    fun dark() = theme(Theme.Dark)
    fun light() = theme(Theme.Light)
    fun theme(theme: Theme) {
        _theme.value = theme
    }

    private val _errors: MutableStateFlow<ErrorViewer.Error> = MutableStateFlow(error())
    val errors = _errors.asStateFlow()
    override fun showError(error: ErrorViewer.Error) {
        _errors.value = error
    }
}