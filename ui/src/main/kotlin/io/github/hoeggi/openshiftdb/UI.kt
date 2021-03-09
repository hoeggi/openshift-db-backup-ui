package io.github.hoeggi.openshiftdb

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem
import io.github.hoeggi.openshiftdb.ui.composables.Loading
import io.github.hoeggi.openshiftdb.ui.composables.SecretsChooser
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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

class UI {
    val logger = LoggerFactory.getLogger(UI::class.java)

    fun show(port: Int, globalState: GlobalState, onClose: (() -> Unit)) {
        logger.info("starting ui")

        Window(
            title = APP_NAME,
            undecorated = false,
            size = IntSize(1280, 1024),
            events = WindowEvents(
                onClose = onClose
            ),
            menuBar = MenuBar(
                Menu("File",
                    MenuItem(
                        name = "Settings",
                        onClick = { globalState.toggleDrawer() },
                        shortcut = KeyStroke(Key.S)
                    ),
                    MenuItem(
                        name = "Close",
                        onClick = { AppManager.exit() },
                        shortcut = KeyStroke(Key.F4)
                    )
                ),
                Menu("View",
                    MenuItem(
                        name = "Main",
                        onClick = { globalState.main() },
                        shortcut = KeyStroke(Key.M)
                    ),
                    MenuItem(
                        name = "Restore",
                        onClick = { globalState.restore() },
                        shortcut = KeyStroke(Key.R)
                    ),
                    MenuItem(
                        name = "Log",
                        onClick = { globalState.detail() },
                        shortcut = KeyStroke(Key.L)
                    )
                ),
                Menu("Help",
                    MenuItem(
                        name = "Reload",
                        onClick = {
                            val refresh = globalState.refresh(GlobalScope)
                            logger.debug("send refresh event $refresh")
                        },
                        shortcut = KeyStroke(Key.U)
                    )
                )
            )
        ) {

            val scope = rememberCoroutineScope()
            val (ocViewModel, postgresViewModel) = viewModels(port, scope, globalState)

            val loginState by ocViewModel.loginState.collectAsState(
                scope.coroutineContext
            )
            ocViewModel.checkLoginState()

            CompositionLocalProvider(
                GlobalState provides globalState,
                OcViewModel provides ocViewModel,
                PostgresViewModel provides postgresViewModel,
            ) {
                ColorMuskTheme(scope) {
                    val navigationState = GlobalState.current
                    val screen by navigationState.screen.collectAsState(scope.coroutineContext)
                    when (screen) {
                        is Screen.Detail -> {
                            Log(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp),
                                coroutineScope = scope
                            )
                        }
                        is Screen.Main -> {
                            when (loginState) {
                                LoginState.LOGGEDIN -> MainScreen(
                                    ocViewModel = ocViewModel,
                                    postgresViewModel = postgresViewModel,
                                )
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

    @Composable
    private fun MainScreen(
        ocViewModel: OcViewModel,
        postgresViewModel: PostgresViewModel,
    ) {
        val globalState = GlobalState.current
        rememberCoroutineScope().launch {
            globalState.refreshTrigger.collect {
                ocViewModel.update()
                postgresViewModel.update()
            }
        }

        ocViewModel.update()
        postgresViewModel.update()

        Box {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp),
            ) {
                OcPane()
                PostgresPane()
            }
            SecretsChooser(Modifier.align(Alignment.Center), postgresViewModel)
        }
    }
}
