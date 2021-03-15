package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
internal fun RestoreData(modifier: Modifier = Modifier) {

    val viewModel = PostgresViewModel.current
    val info by viewModel.collectAsState(viewModel.restoreInfo)
    val command by viewModel.collectAsState(viewModel.restoreCommand)

    Column(modifier = modifier.padding(10.dp)) {
        LazyColumn {
            items(info) {
                Text(text = it, style = MaterialTheme.typography.body2)
            }
        }
        Divider(Modifier.padding(vertical = 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Restore Command:")
            Icon(Icons.Outlined.ContentCopy, "",
                modifier = Modifier.padding(horizontal = 4.dp)
                    .size(16.dp)
                    .clickable {
                        Toolkit.getDefaultToolkit()
                            .systemClipboard.setContents(StringSelection(command.command), null)
                    }
            )
        }
        SelectionContainer {
            Text(
                modifier = Modifier.padding(vertical = 5.dp),
                text = command.command,
                style = MaterialTheme.typography.caption
            )
        }
        Text(text = "Explanation", modifier = Modifier.padding(top = 10.dp))
        val explanation =
            if (command.existing) "Database * ${command.database} * already exists, using '-c (--clean)' flag to clean/drop before restoring backup"
            else "Database * ${command.database} * does not exist, using ${command.defaultDatabase} and '-C (--create)' flag to create database"
        Text(
            modifier = Modifier.padding(vertical = 5.dp),
            text = explanation,
            style = MaterialTheme.typography.caption
        )
    }
}