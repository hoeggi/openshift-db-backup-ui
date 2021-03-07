package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.ui.composables.oc.CurrentProject
import io.github.hoeggi.openshiftdb.ui.composables.oc.PortForward
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun RestoreView() {
    val postgresViewModel = PostgresViewModel.current
    val info by postgresViewModel.collectAsState(postgresViewModel.restoreInfo)
    val command by postgresViewModel.collectAsState(postgresViewModel.restoreCommand)
    val path by postgresViewModel.collectAsState(postgresViewModel.restorePath)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp),
    ) {
        BackupPicker()
        Divider()
        if (!path.isNullOrBlank()) {
            Row {
                Column(modifier = Modifier.padding(10.dp).fillMaxWidth(0.7f)) {
                    LazyColumn {
                        items(info) {
                            Text(text = it, style = MaterialTheme.typography.body2)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Restore Command:")
                        Icon(Icons.Outlined.ContentCopy, "",
                            modifier = Modifier.padding(horizontal = 4.dp)
                                .size(16.dp)
                                .clickable {
                                    Toolkit.getDefaultToolkit()
                                        .systemClipboard.setContents(StringSelection(command), null)
                                }
                        )
                    }
                    SelectionContainer {
                        Text(
                            modifier = Modifier.padding(vertical = 5.dp),
                            text = command,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
                Column {
                    User()
                    CurrentProject()
                    PortForward()
                }
            }
        }
    }
}