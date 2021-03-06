@file:Suppress("FunctionName")

package io.github.hoeggi.openshiftdb.ui.theme

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.errorhandler.ErrorViewer
import io.github.hoeggi.openshiftdb.ui.composables.ErrorView
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun ColorMuskTheme(
    coroutineScope: CoroutineScope,
    content: @Composable BoxScope.() -> Unit,
) {
    val viewModel = GlobalState.current
    val dark by viewModel.theme.collectAsState(coroutineScope.coroutineContext)
//    val error by viewModel.errors.collectAsState(coroutineScope.coroutineContext)


    val colors = when (dark) {
        Theme.Dark -> darkColors(
            primary = Color(0xFF3C5EE6),
            secondary = Color(0xFF1b3394),
            background = Color(0xFF242424),
            surface = Color(0xFF242424),
            onBackground = Color(0xFFEEEEEE),
        )
        Theme.Light -> lightColors()
    }
    DesktopMaterialTheme(
        colors = colors,
    ) {
        Surface {
            val state = rememberScaffoldState()
            Scaffold(scaffoldState = state) {
                Box {
                    content()
                    Overlays(coroutineScope, state)
                }
            }
        }
    }
}

@Composable
fun BoxScope.Overlays(coroutineScope: CoroutineScope, state: ScaffoldState) {
    val viewModel = GlobalState.current
    val error by viewModel.errors.collectAsState(coroutineScope.coroutineContext)

    if (error.fired) {
        when (val message = error) {
            is ErrorViewer.Error -> ErrorView(
                message.thread,
                message.throwable,
                Modifier.align(Alignment.Center)
            )
            is ErrorViewer.Warning -> coroutineScope.launch {
                state.snackbarHostState
                    .showSnackbar(message.message)
                viewModel.showWarning(viewModel.empty())
            }
        }
    }
}