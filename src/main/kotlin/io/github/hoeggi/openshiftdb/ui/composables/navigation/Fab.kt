package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.Scope

@Composable
fun Fab(
    modifier: Modifier = Modifier
) {
    val globalState = GlobalState.current
    val settings by globalState.settings.collectAsState(Scope.current.coroutineContext)

    Column(modifier = modifier) {
        if (settings) {
            ThemeChooser()
        }

        FloatingActionButton(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                globalState.toggleSettings()
            },
        ) {
            Icon(Icons.Outlined.Settings, "")
        }
    }
}

@Composable
fun ThemeChooser() {
    val globalState = GlobalState.current
    val dark by globalState.theme.collectAsState(Scope.current.coroutineContext)
    Row {
        Text(
            modifier = Modifier.padding(4.dp),
            text = "Theme:"
        )
        Column {
            Row {
                RadioButton(
                    modifier = Modifier.padding(4.dp),
                    selected = dark == Theme.Dark,
                    onClick = {
                        globalState.toggleTheme()
                    },
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "dark"
                )
            }
            Row {
                RadioButton(
                    modifier = Modifier.padding(4.dp),
                    selected = dark == Theme.Light,
                    onClick = {
                        globalState.toggleTheme()
                    },
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "light"
                )
            }
        }
    }
}