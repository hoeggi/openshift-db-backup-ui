package io.github.hoeggi.openshiftdb

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.SYSLOG_LABEL
import io.github.hoeggi.openshiftdb.settings.SettingsProvider
import io.github.hoeggi.openshiftdb.ui.composables.ColorMapping
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun Log(
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    val logs = ViewModelProvider.current.syslogViewModel
    val settings = SettingsProvider()
    val logLines by logs.syslog.collectAsState(coroutineScope.coroutineContext)
    val logLevel by settings.logLevel.collectAsState(coroutineScope.coroutineContext)

    val filteredLines = logLines.filter { logLevel.predicate(it.logLevel) }

    Column(modifier = modifier) {
        Text(
            text = MessageProvider.message(SYSLOG_LABEL),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(10.dp)
        )
        LazyColumn(
            modifier = Modifier
                .padding(10.dp),
            state = LazyListState(filteredLines.size, 0) // listState
        ) {
            items(filteredLines) { item ->
                Text(
                    text = AnnotatedString(
                        text = item.line,
                        spanStyle = SpanStyle(ColorMapping.colors[item.logLevel] ?: Color.White)
                    ),
                    style = MaterialTheme.typography.caption
                        .copy(fontFamily = FontFamily.Monospace)
                )
            }
        }
    }
}
