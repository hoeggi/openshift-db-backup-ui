package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.collectAsState
import javax.swing.JFileChooser

@ExperimentalFoundationApi
@Composable
fun DatabaseChooser() {
    val viewModel = PostgresViewModel.current
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
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val window = LocalAppWindow.current.window
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "",
                    modifier = Modifier.clickable {
                        val chooser = JFileChooser().apply {
                            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

                        }
                        val returnVal = chooser.showOpenDialog(window)
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            viewModel.dumpPath(chooser.selectedFile.absolutePath)
                        }
                    }
                )
                Text(
                    text = dumpPath,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
        }
    }
}
