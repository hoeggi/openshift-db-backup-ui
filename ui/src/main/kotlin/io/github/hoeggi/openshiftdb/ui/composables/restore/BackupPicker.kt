package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.UIScope
import io.github.hoeggi.openshiftdb.api.response.DatabaseRestoreMessage
import io.github.hoeggi.openshiftdb.collectAsState
import java.awt.FileDialog
import javax.swing.JFileChooser


@Composable
internal fun BackupPicker() {
    val postgresViewModel = PostgresViewModel.current
    val window = LocalAppWindow.current.window

    val path by postgresViewModel.collectAsState(postgresViewModel.restorePath)
    val restoreState by postgresViewModel.collectAsState(postgresViewModel.restoreState)

    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = path.isNotEmpty(),
                onClick = {
                    postgresViewModel.restoreDatabase()
                }
            ) {
                Text("Restore Database")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    val chooser = JFileChooser(path).apply {
                        fileSelectionMode = JFileChooser.FILES_ONLY
                    }
                    val returnVal = chooser.showOpenDialog(window)
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        postgresViewModel.updateRestorePath(chooser.selectedFile.absolutePath)
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