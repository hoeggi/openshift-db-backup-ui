package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage

@Composable
fun ConsoleOutput() {
    val viewModel = PostgresViewModel.current
    val text by viewModel.databases.collectAsState("", Scope.current.coroutineContext)
    val downloadProgress: List<DatabaseDownloadMessage.InProgressMessage> by viewModel.downloadProgress
        .collectAsState(Scope.current.coroutineContext)

    Text(
        text = text,
        style = MaterialTheme.typography.caption.copy(
            fontFamily = FontFamily.Monospace
        ),
        modifier = Modifier.fillMaxWidth(1f).padding(10.dp)
    )

    if (downloadProgress.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
        ) {
            items(downloadProgress) { item ->
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.overline
                )
            }
        }
    }
}