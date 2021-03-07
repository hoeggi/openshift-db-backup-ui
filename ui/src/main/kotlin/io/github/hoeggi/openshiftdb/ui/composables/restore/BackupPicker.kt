package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import io.github.hoeggi.openshiftdb.collectAsState
import javax.swing.JFileChooser

@Composable
fun BackupPicker() {
    val postgresViewModel = PostgresViewModel.current
    val window = LocalAppWindow.current.window
    val path by postgresViewModel.collectAsState(postgresViewModel.restorePath)
    Row(
        modifier = Modifier.padding(10.dp).clickable {
            val chooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.FILES_ONLY
            }
            val returnVal = chooser.showOpenDialog(window)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                postgresViewModel.updateRestorePath(chooser.selectedFile.absolutePath)
            }
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = "",
        )
        Text(
            text = path,
            modifier = Modifier.padding(horizontal = 5.dp)
        )
    }
}