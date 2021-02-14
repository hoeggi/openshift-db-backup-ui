package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Column
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
import io.github.hoeggi.openshiftdb.process.Postgres
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun ConsoleOutput() {
    val viewModel = PostgresViewModel.current
    val text by viewModel.databases.collectAsState("", Scope.current.coroutineContext)
    val downloadState: Postgres.PostgresResult.Download by viewModel.downloadState
        .filterIsInstance<Postgres.PostgresResult.Download.InProgres>()
        .collectAsState(
            Postgres.PostgresResult.Download.Unspecified,
            Scope.current.coroutineContext
        )

    Text(
        text = text,
        style = MaterialTheme.typography.caption.copy(
            fontFamily = FontFamily.Monospace
        ),
        modifier = Modifier.fillMaxWidth(1f).padding(10.dp)
    )

    when (downloadState) {
        is Postgres.PostgresResult.Download.InProgres -> {
            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                LazyColumn {
                    val lines = (downloadState as Postgres.PostgresResult.Download.InProgres).lines
                    items(lines) { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.overline
                        )
                    }
                }
            }
        }
    }
}