package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
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
import io.github.hoeggi.openshiftdb.settings.ExportFormat
import io.github.hoeggi.openshiftdb.settings.LogLevels
import io.github.hoeggi.openshiftdb.settings.Theme
import kotlinx.coroutines.CoroutineScope

@Composable
fun LogLevelChooser(coroutineScope: CoroutineScope) {
    val globalState = GlobalState.current
    val level by globalState.logLevel.collectAsState(coroutineScope.coroutineContext)

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = "Log Level"
        )
        LazyColumn {
            items(LogLevels) {
                Row(
                    modifier = Modifier.clickable {
                        globalState.updateLogLevel(it)
                    }
                ) {
                    RadioButton(
                        modifier = Modifier.padding(4.dp),
                        selected = it == level,
                        onClick = null,
                    )
                    Text(
                        modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.body2,
                        text = it.name
                    )
                }
            }
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
        Row(
            modifier = Modifier.clickable {
                globalState.themeDark()
            }
        ) {
            RadioButton(
                modifier = Modifier.padding(4.dp),
                selected = dark == Theme.Dark,
                onClick = null,
            )
            Text(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.body2,
                text = MessageProvider.message(THEME_DARK)
            )
        }
        Row(
            modifier = Modifier.clickable {
                globalState.themeLight()
            }
        ) {
            RadioButton(
                modifier = Modifier.padding(4.dp),
                selected = dark == Theme.Light,
                onClick = null,
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
        Row(
            modifier = Modifier.clickable {
                globalState.updateExportFormat(ExportFormat.Custom)
            }
        ) {
            RadioButton(
                modifier = Modifier.padding(4.dp),
                selected = format == ExportFormat.Custom,
                onClick = null,
            )
            Text(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.body2,
                text = MessageProvider.message(POSTGRES_EXPORT_FORMAT_CUSTOM)
            )
        }
        Row(
            modifier = Modifier.clickable {
                globalState.updateExportFormat(ExportFormat.Plain)
            }
        ) {
            RadioButton(
                modifier = Modifier.padding(4.dp),
                selected = format == ExportFormat.Plain,
                onClick = null,
            )
            Text(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
                style = MaterialTheme.typography.body2,
                text = MessageProvider.message(POSTGRES_EXPORT_FORMAT_PLAIN)
            )
        }
    }
}