@file:Suppress("FunctionName")

package io.github.hoeggi.openshiftdb.ui.theme

import androidx.compose.animation.Crossfade
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.lightColors
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.LoginScreen
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.settings.SettingsProvider
import io.github.hoeggi.openshiftdb.settings.Theme
import io.github.hoeggi.openshiftdb.ui.composables.ErrorView
import io.github.hoeggi.openshiftdb.ui.composables.Loading
import io.github.hoeggi.openshiftdb.ui.composables.navigation.AppErrorViewer
import io.github.hoeggi.openshiftdb.ui.composables.navigation.BottomNav
import io.github.hoeggi.openshiftdb.ui.composables.navigation.CustomErrorViewer
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Drawer
import io.github.hoeggi.openshiftdb.ui.composables.navigation.NavigationProvider
import io.github.hoeggi.openshiftdb.viewmodel.models.LoginState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun Theme(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit,
) {
    val settings = SettingsProvider()
    val dark by settings.theme.collectAsState(coroutineScope.coroutineContext)

    val colors = when (dark) {
        Theme.Dark -> darkColors(
            primary = Color(0xFFFF3C5EE6),
            secondary = Color(0xFFFF1b3394),
            background = Color(0xFF242424),
            surface = Color(0xFF242424),
        )
        Theme.Light -> lightColors(
            primary = Color(0xFFFF3C5EE6),
            secondary = Color(0xFFFF1b3394),
            background = Color(0xFFF2F2F2),
            surface = Color(0xFFF2F2F2),
        )
    }
    DesktopMaterialTheme(
        colors = colors,
    ) {
        Surface {
            content()
        }
    }
}

@Composable
internal fun BaseView(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable BoxScope.() -> Unit,
) {
    val navigator = NavigationProvider()
    val ocViewModel = ViewModelProvider.current.ocViewModel
    val postgresViewModel = ViewModelProvider.current.postgresViewModel

    val settings by navigator.showDrawer.collectAsState(coroutineScope.coroutineContext)
    val loginState by ocViewModel.collectAsState(ocViewModel.loginState)
    val state = rememberScaffoldState()

    ocViewModel.checkLoginState()

    Scaffold(
        scaffoldState = state,
        bottomBar = {
            if (loginState == LoginState.LOGGEDIN) {
                BottomNav(
                    coroutineScope = coroutineScope
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigator.toggleDrawer()
                },
            ) {
                Crossfade(targetState = settings) {
                    val icon = when {
                        settings -> Icons.Outlined.ArrowForward
                        else -> Icons.Outlined.Settings
                    }
                    Icon(icon, "")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = true,
    ) {
        Box {
            when (loginState) {
                LoginState.LOGGEDIN -> {
                    remember { ocViewModel.update() }
                    remember { postgresViewModel.update() }
                    Box(modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp)) {
                        content()
                    }
                }
                LoginState.NOT_LOGGEDIN -> LoginScreen()
                LoginState.UNCHECKED -> Loading()
            }

            val padding = if (loginState == LoginState.LOGGEDIN) 48.dp else 0.dp
            Drawer(Modifier.padding(bottom = padding), settings, coroutineScope) {
                navigator.hideDrawer()
            }
            Overlays(coroutineScope, state)
        }
    }
}

@Composable
internal fun Overlays(coroutineScope: CoroutineScope, state: ScaffoldState) {
    val errorViewer = AppErrorViewer()
    val error by errorViewer.errors.collectAsState(coroutineScope.coroutineContext)

    if (error.fired) {
        when (val message = error) {
            is ErrorViewer.Error -> ErrorView(
                message.thread,
                message.throwable
            )
            is ErrorViewer.Warning -> coroutineScope.launch {
                if (state.snackbarHostState.currentSnackbarData == null) {
                    state.snackbarHostState.showSnackbar(
                        message = message.message,
                        duration = if (message.message.length > 50) SnackbarDuration.Long else SnackbarDuration.Short
                    )
                }
                errorViewer.showWarning(errorViewer.empty())
            }
            is CustomErrorViewer.CustomOverlay -> message.overlay()
        }
    }
}
