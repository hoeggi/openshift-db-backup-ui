package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.collectAsState

@Composable
internal fun ConsoleOutput() {
    val viewModel = ViewModelProvider.current.postgresViewModel
    val text by viewModel.collectAsState(viewModel.databases)
    val downloadProgress by viewModel.collectAsState(viewModel.downloadProgress)
    val resultState by viewModel.collectAsState(viewModel.downloadState)

    if (!downloadProgress.isNullOrEmpty() || text.isNotBlank())
        Surface(
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(Dp.Hairline, MaterialTheme.colors.onSurface),
            elevation = 8.dp,
            modifier = Modifier.padding(10.dp).fillMaxSize()
        ) {
            Column {
                if (text.isNotBlank()) {
                    Box {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.caption.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .padding(top = 10.dp)
                        )
                        Icon(
                            Icons.Outlined.Close, "",
                            modifier = Modifier.size(24.dp)
                                .padding(6.dp)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    viewModel.clearDatabaseText()
                                }
                        )
                    }
                }

                if (resultState is DatabaseDownloadMessage.StartMessage) {
                    viewModel.clearDatabaseText()
                }
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
        }
}