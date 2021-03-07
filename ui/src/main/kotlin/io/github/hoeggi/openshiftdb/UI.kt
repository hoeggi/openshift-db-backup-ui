package io.github.hoeggi.openshiftdb

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ui.composables.SecretsChooser
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Screen
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.theme.ColorMuskTheme
import io.github.hoeggi.openshiftdb.viewmodel.LoginState
import io.github.hoeggi.openshiftdb.viewmodel.OcViewModel
import io.github.hoeggi.openshiftdb.viewmodel.PostgresViewModel
import io.github.hoeggi.openshiftdb.viewmodel.viewModels
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.util.concurrent.Executors

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
