package io.github.hoeggi.openshiftdb.ui

import androidx.compose.desktop.AppManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuItem
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import kotlinx.coroutines.GlobalScope

fun MenuBar(globalState: GlobalState) = androidx.compose.ui.window.MenuBar(
    Menu("File",
        MenuItem(
            name = "Settings",
            onClick = { globalState.toggleDrawer() },
            shortcut = KeyStroke(Key.S)
        ),
        MenuItem(
            name = "Close",
            onClick = { AppManager.exit() },
            shortcut = KeyStroke(Key.F4)
        )
    ),
    Menu("View",
        MenuItem(
            name = "Main",
            onClick = { globalState.main() },
            shortcut = KeyStroke(Key.M)
        ),
        MenuItem(
            name = "Restore",
            onClick = { globalState.restore() },
            shortcut = KeyStroke(Key.R)
        ),
        MenuItem(
            name = "Log",
            onClick = { globalState.detail() },
            shortcut = KeyStroke(Key.L)
        )
    ),
    Menu("Help",
        MenuItem(
            name = "Reload",
            onClick = {
                val refresh = globalState.refresh(GlobalScope)
            },
            shortcut = KeyStroke(Key.U)
        )
    )
)