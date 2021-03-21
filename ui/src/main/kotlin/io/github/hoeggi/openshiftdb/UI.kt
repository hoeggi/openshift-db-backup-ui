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
import io.github.hoeggi.openshiftdb.ui.MenuBar
import io.github.hoeggi.openshiftdb.ui.composables.PanelState
import io.github.hoeggi.openshiftdb.ui.composables.SecretsChooser
import io.github.hoeggi.openshiftdb.ui.composables.VerticalSplittable
import io.github.hoeggi.openshiftdb.ui.composables.eventlog.EventLog
import io.github.hoeggi.openshiftdb.ui.composables.navigation.AppErrorViewer
import io.github.hoeggi.openshiftdb.ui.composables.navigation.MenuControlProvider
import io.github.hoeggi.openshiftdb.ui.composables.navigation.NavigationProvider
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Screen
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.composables.restore.RestoreView
import io.github.hoeggi.openshiftdb.ui.theme.Theme
import io.github.hoeggi.openshiftdb.viewmodel.ViewModelProvider
import io.github.hoeggi.openshiftdb.viewmodel.viewModels
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

const val APP_NAME = "Openshift DB Backup GUI"

internal val ViewModelProvider = staticCompositionLocalOf<ViewModelProvider> {
    error("unexpected call to ViewModelProvider")
}

class UI {
    private val logger = LoggerFactory.getLogger(UI::class.java)

    fun show(port: Int, onClose: (() -> Unit)) {
        logger.info("starting ui")
        val errorViewer = AppErrorViewer()
        setExceptionHandler(errorViewer)
        val viewModelProvider = viewModels(
            port,
            errorViewer
        )
        Window(
            title = APP_NAME,
            undecorated = false,
            size = IntSize(1280, 1024),
            events = WindowEvents(
                onClose = {
                    onClose()
                }
            ),
            menuBar = MenuBar(MenuControlProvider())
        ) {

            CompositionLocalProvider(
                ViewModelProvider provides viewModelProvider,
            ) {
                Theme {
                    val scope = rememberCoroutineScope()
                    val screen by NavigationProvider().screen.collectAsState(scope.coroutineContext)

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
    val globalState = MenuControlProvider()
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

