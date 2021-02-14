package io.github.hoeggi.openshiftdb.ui.theme

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ColorMuskTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    DesktopMaterialTheme (
        colors = if (isDark) darkColors(
            primary = Color(0xFF3C5EE6),
            primaryVariant = Color(0xFF3C5EE6),
            secondary =Color(0xFF1b3394),
            background = Color(0xFF242424)
        ) else lightColors()
    ) {
        Surface {
            Column {
                content()
            }
        }
    }
}