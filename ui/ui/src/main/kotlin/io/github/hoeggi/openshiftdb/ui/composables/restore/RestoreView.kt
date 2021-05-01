package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.api.response.DatabaseRestoreMessage
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.ui.composables.navigation.AppErrorViewer
import io.github.hoeggi.openshiftdb.ui.composables.navigation.MenuControlProvider
import io.github.hoeggi.openshiftdb.ui.composables.oc.CurrentProject
import io.github.hoeggi.openshiftdb.ui.composables.oc.PortForward
import io.github.hoeggi.openshiftdb.ui.composables.outsideClickable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
internal fun RestoreView() {
    val globalState = MenuControlProvider()
    val viewModel = ViewModelProvider.current.postgresViewModel
    val path by viewModel.collectAsState(viewModel.restorePath)

    rememberCoroutineScope().launch {
        globalState.refreshTrigger.collect {
            viewModel.updateRestorePath(path)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BackupPicker()
        Divider()
        if (path.isNotBlank()) {
            Row {
                Column(Modifier.fillMaxWidth(0.7f)) {
                    RestoreData()
                    Divider()
                    RestoreLog()
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

@Composable
internal fun RestoreLog() {
    val viewModel = ViewModelProvider.current.postgresViewModel
    val errorViewer = AppErrorViewer()
    val restoreState by viewModel.collectAsState(viewModel.restoreState)
    val restoreProgress by viewModel.collectAsState(viewModel.restoreProgress)
    val listState = rememberLazyListState(restoreProgress.size, 0)

    when (restoreState) {
        is DatabaseRestoreMessage.RequestConfirmation -> {
            errorViewer.showOverlay(
                AppErrorViewer.customOverlay {
                    RestoreWarning(
                        (restoreState as DatabaseRestoreMessage.RequestConfirmation).message,
                        onCancel = {
                            viewModel.cancelRestore()
                            errorViewer.resetOverlay()
                        },
                        onConfirm = {
                            viewModel.confirmeRestore()
                            errorViewer.resetOverlay()
                        }
                    )
                }
            )
        }
    }
    if (restoreProgress.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(Dp.Hairline, MaterialTheme.colors.onSurface),
            elevation = 8.dp,
            modifier = Modifier.padding(10.dp).fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(10.dp)
            ) {
                items(restoreProgress) { item ->
                    Text(
                        text = item.message,
                        style = MaterialTheme.typography.overline
                    )
                }
            }
        }
    }
}

@Composable
internal fun RestoreWarning(
    command: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Box(
        modifier = Modifier.background(MaterialTheme.colors.background.copy(alpha = 0.7f))
            .outsideClickable(onCancel)
            .fillMaxSize()
    ) {
        Surface(
            elevation = 8.dp,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.75f)
                .wrapContentHeight()
                .outsideClickable(),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = "Restoring Database", modifier = Modifier.padding(8.dp))
                Divider()
                Text(text = "Execute Command:", modifier = Modifier.padding(8.dp))
                Text(
                    text = command,
                    style = MaterialTheme.typography.body2.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(8.dp),
                )
                Divider()
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(219, 84, 81)),
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        onClick = onConfirm
                    ) {
                        Icon(Icons.Outlined.FlashOn, "", modifier = Modifier.size(16.dp))
                        Text(text = "Confirm")
                        Icon(Icons.Outlined.FlashOn, "", modifier = Modifier.size(16.dp))
                    }
                    Button(
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        onClick = onCancel
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}
