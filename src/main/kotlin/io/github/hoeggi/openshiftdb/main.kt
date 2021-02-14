package io.github.hoeggi.openshiftdb

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.plus
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.process.OC
import io.github.hoeggi.openshiftdb.process.Postgres
import io.github.hoeggi.openshiftdb.process.ProcessResult
import io.github.hoeggi.openshiftdb.ui.composables.oc.OcPane
import io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresPane
import io.github.hoeggi.openshiftdb.ui.theme.ColorMuskTheme
import io.github.hoeggi.openshiftdb.ui.viewmodel.OcViewModel
import io.github.hoeggi.openshiftdb.ui.viewmodel.PostgresViewModel
import kotlinx.coroutines.*
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
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

val Scope = compositionLocalOf<CoroutineScope>()
val PostgresViewModel = compositionLocalOf<PostgresViewModel>()
val OcViewModel = compositionLocalOf<OcViewModel>()
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
            OC.OcResult.LoginState.NotLogedIn(ProcessResult.Unset),
            scope.coroutineContext
        )

        var dark by mutableStateOf(true)
        var tabbed by mutableStateOf(false)

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
//                    Text(text = "Tabbed UI")
//                    Switch(
//                        checked = tabbed,
//                        onCheckedChange = {
//                            tabbed = !tabbed
//                        }
//                    )
                }
                when (loginState) {
                    is OC.OcResult.LoginState.LoggedIn -> MainScreen(
                        ocViewModel = ocViewModel,
                        postgresViewModel = postgresViewModel,
                        tabbedUI = tabbed
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

var token by mutableStateOf("")
var selected by mutableStateOf(-1)

@Composable
private fun LoginScreen(ocViewModel: OcViewModel) {

    val server by ocViewModel.server.collectAsState(
        listOf(),
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
                OutlinedTextField(
                    modifier = Modifier//.fillMaxWidth(0.75f)
                        .shortcuts {
                            on(Key.CtrlLeft + Key.V) {
                                val text = Toolkit.getDefaultToolkit()
                                    .systemClipboard.getData(DataFlavor.stringFlavor)?.toString()
                                if (text.isNullOrEmpty().not()) token = text!!
                            }
                        },
                    value = token,
                    onValueChange = {
                        token = it
                    },
                    label = { Text("Token") },
                )
                Button(
                    onClick = {
                        ocViewModel.login(token, server[selected].server)
                    },
                    enabled = token.isNotEmpty() && selected != -1,
                    modifier = Modifier.padding(4.dp),
                ) {
                    Text(text = "Login")
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
    tabbedUI: Boolean
) {
    ocViewModel.update()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
    ) {

        if (tabbedUI) {
            var selected by mutableStateOf(0)
            TabRow(
                selectedTabIndex = selected
            ) {
                Tab(
                    selected = selected == 0,
                    onClick = {
                        selected = 0
                    }
                ) {
                    CompositionLocalProvider(OcViewModel provides ocViewModel) {
                        OcPane(modifier = Modifier.fillMaxWidth())
                    }
                }
                Tab(
                    selected = selected == 1,
                    onClick = {
                        selected = 1
                    }
                ) {
                    CompositionLocalProvider(PostgresViewModel provides postgresViewModel) {
                        PostgresPane()
                    }
                }
            }

        } else {
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
}

