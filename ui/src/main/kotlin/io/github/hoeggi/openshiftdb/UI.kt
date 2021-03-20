package io.github.hoeggi.openshiftdb

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.errorhandler.ExceptionHandler
import io.github.hoeggi.openshiftdb.settings.Settings
import io.github.hoeggi.openshiftdb.settings.SettingsProvider
import io.github.hoeggi.openshiftdb.ui.MenuBar
import io.github.hoeggi.openshiftdb.ui.composables.PanelState
import io.github.hoeggi.openshiftdb.ui.composables.SecretsChooser
import io.github.hoeggi.openshiftdb.ui.composables.VerticalSplittable
import io.github.hoeggi.openshiftdb.ui.composables.eventlog.EventLog
import io.github.hoeggi.openshiftdb.ui.composables.navigation.*
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.composables.restore.RestoreView
import io.github.hoeggi.openshiftdb.ui.theme.Theme
import io.github.hoeggi.openshiftdb.viewmodel.ViewModelProvider
import io.github.hoeggi.openshiftdb.viewmodel.viewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.slf4j.LoggerFactory

const val APP_NAME = "Openshift DB Backup GUI"

internal val ViewModelProvider = staticCompositionLocalOf<ViewModelProvider> {
    error("unexpected call to ViewModelProvider")
}
internal val AppErrorViewer = staticCompositionLocalOf<CustomErrorViewer> {
    error("unexpected call to GlobalState")
}
internal val AppNavigator = staticCompositionLocalOf<Navigator> {
    error("unexpected call to GlobalState")
}
internal val AppMenuControl = staticCompositionLocalOf<MenuControl> {
    error("unexpected call to GlobalState")
}
internal val AppLog = staticCompositionLocalOf<LogLines> {
    error("unexpected call to UIScope")
}
internal val AppSettings = staticCompositionLocalOf<Settings> {
    error("unexpected call to UIScope")
}
internal val UIScope = staticCompositionLocalOf<CoroutineScope> {
    error("unexpected call to UIScope")
}

class UI {
    private val logger = LoggerFactory.getLogger(UI::class.java)

    fun show(port: Int, onClose: (() -> Unit)) {
        logger.info("starting ui")
        val supervisor = SupervisorJob()
        setExceptionHandler(ErrorViewProvider.instance())

        Window(
            title = APP_NAME,
            undecorated = false,
            size = IntSize(1280, 1024),
            events = WindowEvents(
                onClose = {
                    supervisor.cancel()
                    onClose()
                }
            ),
            menuBar = MenuBar(MenuControlProvider.instance())
        ) {

            val scope = rememberCoroutineScope() + supervisor
            val viewModelProvider = viewModels(port,
                scope,
                ErrorViewProvider.instance())

            CompositionLocalProvider(
                UIScope provides scope,
                ViewModelProvider provides viewModelProvider,
                AppErrorViewer provides ErrorViewProvider.instance(),
                AppNavigator provides NavigatorProvider.instance(),
                AppMenuControl provides MenuControlProvider.instance(),
                AppLog provides LogLinesProvider.instance(),
                AppSettings provides SettingsProvider.instance(),
            ) {
                Theme(scope) {
                    val navigationState = AppNavigator.current
                    val screen by navigationState.screen.collectAsState(scope.coroutineContext)

                    Crossfade(targetState = screen) {
                        when (it) {
                            is Screen.Detail -> Log()
                            is Screen.Main -> MainScreen()
                            is Screen.Restore -> RestoreView()
                            is Screen.Events -> EventLog()
                        }
                    }
                }
            }
        }
    }

    private fun setExceptionHandler(errorViewer: ErrorViewer) {
        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(errorViewer, defaultUncaughtExceptionHandler))
    }
}

@Composable
private fun MainScreen() {

    val initialWidth = (LocalAppWindow.current.width / 2).dp
    val panelState = remember { PanelState(initialWidth) }

    val animatedSize by animateDpAsState(
        panelState.expandedSize,
        SpringSpec(stiffness = Spring.StiffnessHigh)
    )

    val viewModelProvider = ViewModelProvider.current
    val globalState = AppMenuControl.current
    rememberCoroutineScope().launch {
        globalState.refreshTrigger.collect {
            viewModelProvider.ocViewModel.update()
            viewModelProvider.postgresViewModel.update()
        }
    }

    VerticalSplittable(
        Modifier.fillMaxSize(),
        panelState.splitter,
        onResize = {
            panelState.expandedSize =
                (panelState.expandedSize + it).coerceAtLeast(panelState.expandedSizeMin)
        }
    ) {
        OcPane(modifier = Modifier.width(animatedSize))
        PostgresPane()
    }
    SecretsChooser()
}

