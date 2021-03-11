package io.github.hoeggi.openshiftdb

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
import io.github.hoeggi.openshiftdb.ui.MenuBar
import io.github.hoeggi.openshiftdb.ui.composables.Loading
import io.github.hoeggi.openshiftdb.ui.composables.PanelState
import io.github.hoeggi.openshiftdb.ui.composables.SecretsChooser
import io.github.hoeggi.openshiftdb.ui.composables.VerticalSplittable
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Screen
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.composables.restore.RestoreView
import io.github.hoeggi.openshiftdb.ui.theme.ColorMuskTheme
import io.github.hoeggi.openshiftdb.viewmodel.LoginState
import io.github.hoeggi.openshiftdb.viewmodel.OcViewModel
import io.github.hoeggi.openshiftdb.viewmodel.PostgresViewModel
import io.github.hoeggi.openshiftdb.viewmodel.viewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.slf4j.LoggerFactory

const val APP_NAME = "Openshift DB Backup GUI"

val PostgresViewModel = staticCompositionLocalOf<PostgresViewModel> {
    error("unexpected call to PostgresViewModel")
}
val OcViewModel = staticCompositionLocalOf<OcViewModel> {
    error("unexpected call to OcViewModel")
}
val GlobalState = staticCompositionLocalOf<GlobalState> {
    error("unexpected call to GlobalState")
}

val UIScope = staticCompositionLocalOf<CoroutineScope> {
    error("unexpected call to UIScope")
}

class UI {
    val logger = LoggerFactory.getLogger(UI::class.java)

    fun show(port: Int, globalState: GlobalState, onClose: (() -> Unit)) {
        logger.info("starting ui")
        val supervisor = SupervisorJob()
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
            menuBar = MenuBar(globalState)
        ) {

            val scope = rememberCoroutineScope() + SupervisorJob()
            val (ocViewModel, postgresViewModel) = viewModels(port, scope, globalState)

//            val loginState by ocViewModel.collectAsState(ocViewModel.loginState)
            ocViewModel.checkLoginState()

//            val initialLoginState by remember { mutableStateOf(loginState) }

            CompositionLocalProvider(
                UIScope provides scope,
                GlobalState provides globalState,
                OcViewModel provides ocViewModel,
                PostgresViewModel provides postgresViewModel,
            ) {
                ColorMuskTheme(scope) {
                    val navigationState = GlobalState.current
                    val screen by navigationState.screen.collectAsState(scope.coroutineContext)
                    val loginState by ocViewModel.collectAsState(ocViewModel.loginState)
                    when (screen) {
                        is Screen.Detail -> {
                            Log()
                        }
                        is Screen.Main -> {
                            when (loginState) {
                                LoginState.LOGGEDIN -> {
                                    ocViewModel.update()
                                    postgresViewModel.update()
                                    MainScreen(
                                        ocViewModel = ocViewModel,
                                        postgresViewModel = postgresViewModel,
                                    )
                                }
                                LoginState.NOT_LOGGEDIN -> LoginScreen(
                                    ocViewModel = ocViewModel
                                )
                                LoginState.UNCHECKED -> Loading()
                            }
                        }
                        is Screen.Restore -> {
                            RestoreView()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScreen(
    ocViewModel: OcViewModel,
    postgresViewModel: PostgresViewModel,
) {
    val initialWidth = (LocalAppWindow.current.width / 2).dp
    val panelState = remember { PanelState(initialWidth) }

    val animatedSize by animateDpAsState(
        panelState.expandedSize,
        SpringSpec(stiffness = Spring.StiffnessHigh)
    )

    val globalState = GlobalState.current
    rememberCoroutineScope().launch {
        globalState.refreshTrigger.collect {
            ocViewModel.update()
            postgresViewModel.update()
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
//    Row {
//        OcPane()
//        PostgresPane()
//    }
    SecretsChooser(viewModel = postgresViewModel)
}

