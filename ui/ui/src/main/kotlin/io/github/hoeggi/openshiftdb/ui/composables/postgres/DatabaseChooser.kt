package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.ui.composables.pathchooser.FileChooser

@Composable
internal fun DatabaseChooser() {
    val viewModel = ViewModelProvider.current.postgresViewModel
    val selectedDatabase by viewModel.collectAsState(viewModel.selectedDatabase)
    val databases by viewModel.collectAsState(viewModel.databasesLines)
    val dumpPath by viewModel.collectAsState(viewModel.dumpPath)

    Column {
        LazyVerticalGrid(
            modifier = Modifier
                .padding(10.dp),
            cells = GridCells.Fixed(3),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(databases.size) { idx ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(0.75f),
                        text = databases[idx],
                        style = MaterialTheme.typography.caption
                    )
                    RadioButton(
                        selected = idx == selectedDatabase,
                        onClick = {
                            viewModel.updateSelectedDatabase(idx)
                        }
                    )
                }
            }
        }

        if (selectedDatabase != -1) {
            Row(
                modifier = Modifier.padding(10.dp).clickable {
                    FileChooser(dumpPath, false) {
                        viewModel.dumpPath(it.absolutePath)
                    }
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "",
                )
                Text(
                    text = dumpPath,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
        }
    }
}
