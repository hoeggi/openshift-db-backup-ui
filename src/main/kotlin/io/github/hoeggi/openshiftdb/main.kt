package io.github.hoeggi.openshiftdb

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.process.OC
import io.github.hoeggi.openshiftdb.process.Postgres
import io.github.hoeggi.openshiftdb.ui.composables.EditTextField
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.theme.ColorMuskTheme
import io.github.hoeggi.openshiftdb.ui.viewmodel.OcViewModel
import io.github.hoeggi.openshiftdb.ui.viewmodel.PostgresViewModel
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

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
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

        var dark by mutableStateOf(true)

        CompositionLocalProvider(
            Scope provides scope,
        ) {

            ColorMuskTheme(
                isDark = dark
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(text = "Dark Theme: ")
                    Switch(
                        checked = dark,
                        onCheckedChange = {
                            dark = !dark
                        }
                    )
                }
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

var token by mutableStateOf(TextFieldValue(""))
var selected by mutableStateOf(-1)

@Composable
private fun LoginScreen(ocViewModel: OcViewModel) {

    val server by ocViewModel.server.collectAsState(
        Scope.current.coroutineContext
    )
    ocViewModel.listServer()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
    ) {
        Column(
            modifier = Modifier.wrapContentSize(
                align = Alignment.Center
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditTextField(
                    value = token,
                ) {
                    token = it
                }
                Button(
                    onClick = {
                        ocViewModel.login(token.text, server[selected].server)
                    },
                    enabled = token.text.isNotEmpty() && selected != -1,
                    modifier = Modifier.padding(4.dp),
                ) {
                    Text(text = "Login")
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                items(server.size) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            modifier = Modifier.padding(4.dp),
                            selected = it == selected,
                            onClick = {
                                selected = it
                            },
                        )
                        Text(
                            modifier = Modifier.clickable {
                                selected = it
                            },
                            text = server[it].server,
                            style = MaterialTheme.typography.body2,
                        )
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
    ocViewModel.update()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
    ) {
        Row {
            CompositionLocalProvider(OcViewModel provides ocViewModel) {
                OcPane()
            }
            CompositionLocalProvider(PostgresViewModel provides postgresViewModel) {
                PostgresPane()
            }
        }
    }
}

