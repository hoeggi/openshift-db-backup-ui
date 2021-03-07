package io.github.hoeggi.openshiftdb

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.plus
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem
import io.github.hoeggi.openshiftdb.ui.composables.SecretsChooser
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Screen
import io.github.hoeggi.openshiftdb.ui.composables.oc.CurrentProject
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.oc.PortForward
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.composables.restore.RestoreView
import io.github.hoeggi.openshiftdb.ui.composables.restore.User
import io.github.hoeggi.openshiftdb.ui.theme.ColorMuskTheme
import io.github.hoeggi.openshiftdb.viewmodel.LoginState
import io.github.hoeggi.openshiftdb.viewmodel.OcViewModel
import io.github.hoeggi.openshiftdb.viewmodel.PostgresViewModel
import io.github.hoeggi.openshiftdb.viewmodel.viewModels
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.util.concurrent.Executors
import javax.swing.JFileChooser
import javax.swing.KeyStroke

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
            size = IntSize(1024, 1024),
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
                            CompositionLocalProvider(
                                OcViewModel provides ocViewModel,
                                PostgresViewModel provides postgresViewModel
                            ) {
                                RestoreView()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Loading() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(10.dp))
        }
    }

    @Composable
    private fun MainScreen(
        ocViewModel: OcViewModel,
        postgresViewModel: PostgresViewModel,
    ) {
        ocViewModel.update()
        postgresViewModel.update()
        Box {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp),
            ) {
                CompositionLocalProvider(OcViewModel provides ocViewModel) {
                    OcPane()
                }
                CompositionLocalProvider(PostgresViewModel provides postgresViewModel) {
                    PostgresPane()
                }
            }
            SecretsChooser(Modifier.align(Alignment.Center), postgresViewModel)
        }
    }
}
