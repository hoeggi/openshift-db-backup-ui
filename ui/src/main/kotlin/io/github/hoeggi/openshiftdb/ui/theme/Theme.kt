@file:Suppress("FunctionName")

package io.github.hoeggi.openshiftdb.ui.theme

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.ui.composables.ErrorView
import io.github.hoeggi.openshiftdb.ui.composables.navigation.Theme
import kotlinx.coroutines.CoroutineScope


@Composable
fun ColorMuskTheme(
    coroutineScope: CoroutineScope,
    content: @Composable BoxScope.() -> Unit,
) {
    val viewModel = GlobalState.current
    val dark by viewModel.theme.collectAsState(coroutineScope.coroutineContext)
    val error by viewModel.errors.collectAsState(coroutineScope.coroutineContext)


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
            Box {
                content()
                if (error.fired) {
                    ErrorView(error.thread, error.throwable, Modifier.align(Alignment.Center))
                }
            }
        }
    }
}