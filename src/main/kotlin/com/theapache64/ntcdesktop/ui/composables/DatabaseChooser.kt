package com.theapache64.ntcdesktop.ui.composables

import androidx.compose.desktop.LocalAppWindow
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theapache64.ntcdesktop.PostgresViewModel
import com.theapache64.ntcdesktop.Scope
import javax.swing.JFileChooser

@Composable
fun DatabaseChooser() {
    val viewModel = PostgresViewModel.current
    val selectedDatabase by viewModel.selectedDatabase.collectAsState(Scope.current.coroutineContext)
    val databases by viewModel.databasesLines.collectAsState(Scope.current.coroutineContext)
    val dumpPath by viewModel.dumpPath.collectAsState(System.getProperty("user.home"), Scope.current.coroutineContext)

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
                        style = MaterialTheme.typography.overline
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
                    text = dumpPath
                )
            }
        }
    }
}

private fun List<String>.get(index: Int, default: String) =
    if (index in 0..size) get(index) else default