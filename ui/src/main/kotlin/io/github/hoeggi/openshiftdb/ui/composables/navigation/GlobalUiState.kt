package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal sealed class Screen {
    object Main : Screen()
    object Detail : Screen()
    object Restore : Screen()
}

internal interface Navigator {
    val screen: StateFlow<Screen>
    fun main()
    fun detail()
    fun restore()
    val showDrawer: StateFlow<Boolean>
    fun toggleDrawer()
    fun hideDrawer()
}

internal interface CustomErrorViewer : ErrorViewer {
    fun resetOverlay()

    fun showOverlay(overlay: CustomOverlay)
    val errors: StateFlow<ErrorViewer.Message>
    interface CustomOverlay : ErrorViewer.Message {
        val overlay: @Composable () -> Unit
    }
}

internal object ErrorViewProvider {
    fun instance(): CustomErrorViewer = GlobalUiState

    fun customOverlay(overlay: @Composable () -> Unit) = object : CustomErrorViewer.CustomOverlay {
        override val overlay = overlay
        override val fired = true
    }
}

internal interface MenuControl : Navigator {
    val refreshTrigger: SharedFlow<Unit>
    fun refresh(coroutineScope: CoroutineScope): Job
}

internal object MenuControlProvider {
    fun instance(): MenuControl = GlobalUiState
}
internal object NavigatorProvider {
    fun instance(): Navigator = GlobalUiState
}
internal object LogLinesProvider {
    fun instance(): LogLines = GlobalUiState
}

internal interface LogLines {
    val syslog: StateFlow<List<AnnotatedString>>
    fun updateSyslog(log: List<AnnotatedString>)
}

private object GlobalUiState : CustomErrorViewer, MenuControl, LogLines {

    private val _refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow()
    override val refreshTrigger = _refreshTrigger.asSharedFlow()
    override fun refresh(coroutineScope: CoroutineScope) = coroutineScope.launch {
        _refreshTrigger.emit(Unit)
    }


    private val _syslog = MutableStateFlow(listOf(AnnotatedString("", SpanStyle()))
    )
    override val syslog = _syslog.asStateFlow()
    override fun updateSyslog(log: List<AnnotatedString>) {
        _syslog.value = log
    }

    private val _screen: MutableStateFlow<Screen> =
        MutableStateFlow(Screen.Main)
    override val screen = _screen.asStateFlow()
    override fun main() = navigateTo(Screen.Main)
    override fun detail() = navigateTo(Screen.Detail)
    override fun restore() = navigateTo(Screen.Restore)
    private fun navigateTo(screen: Screen) {
        _screen.value = screen
    }

    private val _showDrawer: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val showDrawer = _showDrawer.asStateFlow()
    override fun toggleDrawer() {
        _showDrawer.value = !_showDrawer.value
    }

    override fun hideDrawer() {
        _showDrawer.value = false
    }

    private val _errors: MutableStateFlow<ErrorViewer.Message> = MutableStateFlow(empty())
    override val errors = _errors.asStateFlow()

    override fun resetOverlay() {
        _errors.value = empty()
    }

    override fun showOverlay(overlay: CustomErrorViewer.CustomOverlay) {
        _errors.value = overlay
    }

    override fun showError(error: ErrorViewer.Message) {
        _errors.value = error
    }

    override fun showWarning(warning: ErrorViewer.Message) {
        _errors.value = warning
    }

}