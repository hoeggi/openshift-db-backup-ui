package io.github.hoeggi.openshiftdb

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.process.OC
import io.github.hoeggi.openshiftdb.process.Postgres
import io.github.hoeggi.openshiftdb.ui.composables.navigation.BottomNav
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Fab
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Screen
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.theme.ColorMuskTheme
import io.github.hoeggi.openshiftdb.ui.viewmodel.OcViewModel
import io.github.hoeggi.openshiftdb.ui.viewmodel.PostgresViewModel
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import javax.swing.UIManager

const val APP_NAME = "Openshift DB Backup GUI"

class Main

private val backgroundDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
val Dispatchers.BACKGROUND: CoroutineDispatcher
    get() = backgroundDispatcher

private val downloadDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
val Dispatchers.DUMP: CoroutineDispatcher
    get() = downloadDispatcher

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

fun main() {
    val globalState = GlobalState()
    val logger = Logger(globalState)
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(globalState))

    Window(
        title = APP_NAME,
        undecorated = false,
        size = IntSize(1024, 1024),
        events = WindowEvents(
            onClose = {
                ProcessHandle.current()
                    .children()
                    .forEach {
                        it.destroy()
                    }
                logger.stop()
            }
        )
    ) {

        val scope = rememberCoroutineScope() + Dispatchers.BACKGROUND + Dispatchers.DUMP
        val oc = OC()

        val postgres = Postgres()
        val ocViewModel = OcViewModel(oc, scope)
        val postgresViewModel = PostgresViewModel(postgres, oc, scope)

        val loginState: OC.OcResult.LoginState by ocViewModel.loginState.collectAsState(
            scope.coroutineContext
        )

        CompositionLocalProvider(
            Scope provides scope,
            GlobalState provides globalState,
        ) {
            ColorMuskTheme {
                when (loginState) {
                    is OC.OcResult.LoginState.LoggedIn -> MainScreen(
                        ocViewModel = ocViewModel,
                        postgresViewModel = postgresViewModel,
                    )
                    is OC.OcResult.LoginState.NotLogedIn -> LoginScreen(
                        ocViewModel = ocViewModel
                    )
                    is OC.OcResult.LoginState.Unchecked -> Loading(
                        ocViewModel = ocViewModel
                    )
                }
                Fab(
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(bottom = 15.dp, end = 15.dp),
                )
            }
        }
    }

}

@Composable
private fun Loading(ocViewModel: OcViewModel) {

    Scope.current.launch(Dispatchers.BACKGROUND) {
        delay(1000)
        ocViewModel.checkLoginState()
    }

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
    Box {
        Control(ocViewModel, postgresViewModel)
        BottomNav(
            Modifier.align(Alignment.BottomCenter).height(48.dp)
        )
    }
}

@Composable
private fun Control(
    ocViewModel: OcViewModel,
    postgresViewModel: PostgresViewModel,
) {
    val navigationState = GlobalState.current
    val screen by navigationState.screen.collectAsState(Scope.current.coroutineContext)

    when (screen) {
        is Screen.Detail -> {
            Log(modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp),)
        }
        is Screen.Main -> {
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
        }
    }
}