package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_EXPORT_FORMAT_CUSTOM
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_EXPORT_FORMAT_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_EXPORT_FORMAT_PLAIN
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.THEME_DARK
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.THEME_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.THEME_LIGHT
import kotlinx.coroutines.CoroutineScope

@Composable
fun Fab(
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
) {
    val globalState = GlobalState.current
    val settings by globalState.settings.collectAsState(coroutineScope.coroutineContext)


    Column(modifier = modifier) {
        if (settings) {
            Row {
                ExportFormatChooser(coroutineScope)
                ThemeChooser(coroutineScope)
            }
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
fun ThemeChooser(coroutineScope: CoroutineScope) {
    val globalState = GlobalState.current
    val dark by globalState.theme.collectAsState(coroutineScope.coroutineContext)

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = MessageProvider.message(THEME_LABEL)
        )
        Row {
            RadioButton(
                modifier = Modifier.padding(4.dp),
                selected = dark == Theme.Dark,
                onClick = {
                    globalState.toggleTheme()
                },
            )
            Text(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.body2,
                text = MessageProvider.message(THEME_DARK)
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
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.body2,
                text = MessageProvider.message(THEME_LIGHT)
            )
        }
    }
}

@Composable
fun ExportFormatChooser(coroutineScope: CoroutineScope) {
    val globalState = GlobalState.current
    val format by globalState.exportFormat.collectAsState(coroutineScope.coroutineContext)

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = MessageProvider.message(POSTGRES_EXPORT_FORMAT_LABEL)
        )
        Row {
            RadioButton(
                modifier = Modifier.padding(4.dp),
                selected = format == ExportFormat.Custom,
                onClick = {
                    globalState.updateExportFormat(ExportFormat.Custom)
                },
            )
            Text(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.body2,
                text = MessageProvider.message(POSTGRES_EXPORT_FORMAT_CUSTOM)
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
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.body2,
                text = MessageProvider.message(POSTGRES_EXPORT_FORMAT_PLAIN)
            )
        }
    }
}