package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.runtime.Composable
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class Screen {
    object Main : Screen()
    object Detail : Screen()
    object Restore : Screen()
    object Events : Screen()
}

interface Navigator {
    val screen: StateFlow<Screen>
    fun main()
    fun detail()
    fun restore()
    fun events()
    val showDrawer: StateFlow<Boolean>
    fun toggleDrawer()
    fun hideDrawer()
}

interface CustomErrorViewer : ErrorViewer {
    fun resetOverlay()

    fun showOverlay(overlay: CustomOverlay)
    val errors: StateFlow<ErrorViewer.Message>

    interface CustomOverlay : ErrorViewer.Message {
        val overlay: @Composable () -> Unit
    }
}

abstract class Provider<T> {
    abstract val instance: T
    operator fun invoke() = instance
}

object MenuControlProvider : Provider<MenuControl>() {
    override val instance: MenuControl by lazy { GlobalUiState }
}

object NavigationProvider : Provider<Navigator>() {
    override val instance: Navigator by lazy { GlobalUiState }
}

interface MenuControl : Navigator {
    val refreshTrigger: SharedFlow<Unit>
    fun refresh(): Job
}

object AppErrorViewer : Provider<CustomErrorViewer>() {
    override val instance: CustomErrorViewer by lazy { GlobalUiState }

    fun customOverlay(overlay: @Composable () -> Unit) = object : CustomErrorViewer.CustomOverlay {
        override val overlay = overlay
        override val fired = true
    }
}

private object GlobalUiState : CustomErrorViewer, MenuControl {

    private val coroutineScope = MainScope()

    val _refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow()
    override val refreshTrigger = _refreshTrigger.asSharedFlow()
    override fun refresh() = coroutineScope.launch {
        _refreshTrigger.emit(Unit)
    }

    private val _screen: MutableStateFlow<Screen> =
        MutableStateFlow(Screen.Main)
    override val screen = _screen.asStateFlow()
    override fun main() = navigateTo(Screen.Main)
    override fun detail() = navigateTo(Screen.Detail)
    override fun restore() = navigateTo(Screen.Restore)
    override fun events() = navigateTo(Screen.Events)
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
