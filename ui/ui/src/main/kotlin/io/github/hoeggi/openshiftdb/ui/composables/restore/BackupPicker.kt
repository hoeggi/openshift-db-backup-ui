package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.api.response.DatabaseRestoreMessage
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.ui.composables.pathchooser.FileChooser

@Composable
internal fun BackupPicker() {
    val viewModel = ViewModelProvider.current.postgresViewModel

    val path by viewModel.collectAsState(viewModel.restorePath)
    val restoreState by viewModel.collectAsState(viewModel.restoreState)

    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = path.isNotEmpty(),
                onClick = {
                    viewModel.restoreDatabase()
                }
            ) {
                Text("Restore Database")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    FileChooser(path, true) {
                        viewModel.updateRestorePath(it.absolutePath)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "",
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Text(
                    text = path,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
        }
        Box(modifier = Modifier.size(48.dp)) {
            when (restoreState) {
                is DatabaseRestoreMessage.InProgressMessage,
                DatabaseRestoreMessage.StartMessage,
                -> CircularProgressIndicator(modifier = Modifier.padding(10.dp))
            }
        }
    }
}
