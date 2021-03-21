package io.github.hoeggi.openshiftdb.ui

import androidx.compose.desktop.AppManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuItem
import io.github.hoeggi.openshiftdb.ui.composables.navigation.MenuControl
import kotlinx.coroutines.GlobalScope

internal fun MenuBar(menuControl: MenuControl) = androidx.compose.ui.window.MenuBar(
    Menu("File",
        MenuItem(
            name = "Settings",
            onClick = { menuControl.toggleDrawer() },
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
            onClick = { menuControl.main() },
            shortcut = KeyStroke(Key.M)
        ),
        MenuItem(
            name = "Restore",
            onClick = { menuControl.restore() },
            shortcut = KeyStroke(Key.R)
        ),
        MenuItem(
            name = "Events",
            onClick = { menuControl.restore() },
            shortcut = KeyStroke(Key.E)
        ),
        MenuItem(
            name = "Log",
            onClick = { menuControl.detail() },
            shortcut = KeyStroke(Key.L)
        )
    ),
    Menu("Help",
        MenuItem(
            name = "Reload",
            onClick = {
                menuControl.refresh()
            },
            shortcut = KeyStroke(Key.U)
        )
    )
)