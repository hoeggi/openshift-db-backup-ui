@file:Suppress("FunctionName")

package io.github.hoeggi.openshiftdb.ui.theme

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.settings.Theme
import io.github.hoeggi.openshiftdb.ui.composables.ErrorView
import io.github.hoeggi.openshiftdb.ui.composables.navigation.BottomNav
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Drawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@ExperimentalAnimationApi
@Composable
fun ColorMuskTheme(
    coroutineScope: CoroutineScope,
    content: @Composable BoxScope.() -> Unit,
) {
    val globalState = GlobalState.current
    val dark by globalState.theme.collectAsState(coroutineScope.coroutineContext)
    val colors = when (dark) {
        Theme.Dark -> darkColors(
            primary = Color(0xFF3C5EE6),
            secondary = Color(0xFF1b3394),
            background = Color(0xFF242424),
            surface = Color(0xFF242424),
        )
        Theme.Light -> lightColors(
            primary = Color(0xFF3C5EE6),
            secondary = Color(0xFF1b3394),
            background = Color(0xFFF2F2F2),
            surface = Color(0xFFF2F2F2)
        )
    }
    DesktopMaterialTheme(
        colors = colors,
    ) {
        Surface {
            val state = rememberScaffoldState()
            val settings by globalState.showDrawer.collectAsState(coroutineScope.coroutineContext)
            Scaffold(
                scaffoldState = state,
                bottomBar = {
                    BottomNav(
                        coroutineScope = coroutineScope
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            globalState.toggleDrawer()
                        },
                    ) {
                        Icon(Icons.Outlined.Settings, "")
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true,
            ) {
                Box {
                    Box(modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp)) {
                        content()
                    }
                    Drawer(Modifier.padding(bottom = 48.dp), settings, coroutineScope) {
                        globalState.hideDrawer()
                    }
                    Overlays(coroutineScope, state)
                }
            }
        }
    }
}

fun customOverlay(overlay: @Composable () -> Unit) = object : CustomOverlay {
    override val overlay = overlay
    override val fired = true
}

interface CustomOverlay : ErrorViewer.Message {
    val overlay: @Composable () -> Unit
}

@Composable
fun Overlays(coroutineScope: CoroutineScope, state: ScaffoldState) {
    val viewModel = GlobalState.current
    val error by viewModel.errors.collectAsState(coroutineScope.coroutineContext)

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
                viewModel.showWarning(viewModel.empty())
            }
            is CustomOverlay -> message.overlay()
        }
    }
}