package io.github.hoeggi.openshiftdb

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ui.composables.SecretsChooser
import io.github.hoeggi.openshiftdb.ui.composables.navigation.BottomNav
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Fab
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Screen
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.theme.ColorMuskTheme
import io.github.hoeggi.openshiftdb.viewmodel.LoginState
import io.github.hoeggi.openshiftdb.viewmodel.OcViewModel
import io.github.hoeggi.openshiftdb.viewmodel.PostgresViewModel
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

const val APP_NAME = "Openshift DB Backup GUI"

val Scope = staticCompositionLocalOf<CoroutineScope> {
    error("unexpected call to Scope")
}
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

    @ExperimentalFoundationApi
    fun show(port: Int, globalState: GlobalState, onClose: (() -> Unit)) {
        logger.info("starting ui")
        Window(
            title = APP_NAME,
            undecorated = false,
            size = IntSize(1024, 1024),
            events = WindowEvents(
                onClose = onClose
            )
        ) {

            val scope = rememberCoroutineScope()
            val ocViewModel = OcViewModel(port)
            val postgresViewModel = PostgresViewModel(port)

            val loginState by ocViewModel.loginState.collectAsState(
                scope.coroutineContext
            )

            scope.launch(Dispatchers.IO) {
                delay(1000)
                ocViewModel.checkLoginState()
            }
            CompositionLocalProvider(
                Scope provides scope,
                GlobalState provides globalState,
            ) {
                ColorMuskTheme {
                    val navigationState = GlobalState.current
                    val screen by navigationState.screen.collectAsState(Scope.current.coroutineContext)
                    when (screen) {
                        is Screen.Detail -> {
                            Log(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp),
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
                    }
                    BottomNav(
                        Modifier.align(Alignment.BottomCenter).height(48.dp)
                    )
                    Fab(
                        modifier = Modifier.align(Alignment.BottomEnd)
                            .padding(bottom = 15.dp, end = 15.dp),
                    )
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

    @ExperimentalFoundationApi
    @Composable
    private fun MainScreen(
        ocViewModel: OcViewModel,
        postgresViewModel: PostgresViewModel,
    ) {
        Scope.current.launch(Dispatchers.IO) {
            ocViewModel.update()
            postgresViewModel.update()
        }
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
            SecretsChooser(Modifier.align(Alignment.TopCenter), postgresViewModel)
        }
    }
}
