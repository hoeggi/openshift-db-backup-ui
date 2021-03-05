package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.Scope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun Fab(
    modifier: Modifier = Modifier
) {
    val globalState = GlobalState.current
    val settings by globalState.settings.collectAsState(Scope.current.coroutineContext)

    Column(modifier = modifier) {
        if (settings) {
            ExportFormatChooser()
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

@Composable
fun ExportFormatChooser() {
    val globalState = GlobalState.current
    val format by globalState.exportFormat.collectAsState(Scope.current.coroutineContext)
    Row {
        Text(
            modifier = Modifier.padding(4.dp),
            text = "Export Format:"
        )
        Column {
            Row {
                RadioButton(
                    modifier = Modifier.padding(4.dp),
                    selected = format == ExportFormat.Custom,
                    onClick = {
                        globalState.updateExportFormat(ExportFormat.Custom)
                    },
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "custom"
                )
            }
            Row {
                RadioButton(
                    modifier = Modifier.padding(4.dp),
                    selected = format == ExportFormat.Plain,
                    onClick = {
                        globalState.updateExportFormat(ExportFormat.Plain)
                    },
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "plain"
                )
            }
        }
    }
}