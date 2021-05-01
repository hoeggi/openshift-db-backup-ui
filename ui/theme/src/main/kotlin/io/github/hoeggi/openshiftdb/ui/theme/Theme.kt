package io.github.hoeggi.openshiftdb.ui.theme

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import io.github.hoeggi.openshiftdb.settings.SettingsProvider
import io.github.hoeggi.openshiftdb.settings.Theme
import kotlinx.coroutines.CoroutineScope

@Composable
fun Theme(
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
