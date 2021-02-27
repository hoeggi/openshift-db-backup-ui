package io.github.hoeggi.openshiftdb

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ui.composables.ExpandableText
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import javax.swing.UIManager

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

            CompositionLocalProvider(
                Scope provides scope,
                GlobalState provides globalState,
            ) {
                ColorMuskTheme {
                    when (loginState) {
                        LoginState.LOGGEDIN -> MainScreen(
                            ocViewModel = ocViewModel,
                            postgresViewModel = postgresViewModel,
                        )
                        LoginState.NOT_LOGGEDIN -> LoginScreen(
                            ocViewModel = ocViewModel
                        )
                        LoginState.UNCHECKED -> Loading(
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

        Scope.current.launch(Dispatchers.IO) {
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

    @ExperimentalFoundationApi
    @Composable
    private fun MainScreen(
        ocViewModel: OcViewModel,
        postgresViewModel: PostgresViewModel,
    ) {
        val secrets by postgresViewModel.secrets.collectAsState(Scope.current.coroutineContext)
        Scope.current.launch(Dispatchers.IO) {
            ocViewModel.update()
            postgresViewModel.update()
        }
        Box {
            Control(ocViewModel, postgresViewModel)
            BottomNav(
                Modifier.align(Alignment.BottomCenter).height(48.dp)
            )
            if (secrets.isNotEmpty()) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight(0.75f)
                        .padding(16.dp)
                        .background(color = MaterialTheme.colors.background, shape = RoundedCornerShape(6.dp))
                        .border(1.dp, color = Color.White, shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.TopStart,

                    ) {
                    Icon(Icons.Outlined.Close, "",
                        modifier = Modifier.align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clickable {
                                postgresViewModel.clearSecrets()
                            }
                    )
                    LazyColumn(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        items(secrets) { item ->
                            ExpandableText(
                                text = item.name
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                                ) {
                                    item.data.forEach {
                                        Text(
                                            text = "${it.key}:",
                                            style = MaterialTheme.typography.body2
                                        )
                                        Text(
                                            modifier = Modifier.clickable {
                                                postgresViewModel.clearSecrets()
                                                postgresViewModel.updatePassword(it.value)
                                            },
                                            text = "${it.value}",
                                            style = MaterialTheme.typography.caption
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    private fun Control(
        ocViewModel: OcViewModel,
        postgresViewModel: PostgresViewModel,
    ) {
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
}

