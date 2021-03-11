package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.api.response.DatabaseRestoreMessage
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.outsideClickable
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import io.github.hoeggi.openshiftdb.ui.composables.oc.CurrentProject
import io.github.hoeggi.openshiftdb.ui.composables.oc.PortForward
import io.github.hoeggi.openshiftdb.ui.theme.customOverlay
import io.github.hoeggi.openshiftdb.viewmodel.PostgresViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun RestoreView() {
    val globalState = GlobalState.current
    val postgresViewModel = PostgresViewModel.current
    val path by postgresViewModel.collectAsState(postgresViewModel.restorePath)

    rememberCoroutineScope().launch {
        globalState.refreshTrigger.collect {
            postgresViewModel.updateRestorePath(path)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BackupPicker()
        Divider()
        if (!path.isNullOrBlank()) {
            Row {
                Column(Modifier.fillMaxWidth(0.7f)) {
                    RestoreData()
                    Divider()
                    RestoreLog(globalState, postgresViewModel)
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
fun RestoreLog(globalState: GlobalState, postgresViewModel: PostgresViewModel) {
    val restoreState by postgresViewModel.collectAsState(postgresViewModel.restoreState)
    val restoreProgress by postgresViewModel.collectAsState(postgresViewModel.restoreProgress)
    val listState = rememberLazyListState(restoreProgress.size, 0)

    when (restoreState) {
        is DatabaseRestoreMessage.RequestConfirmation -> {
            globalState.showOverlay(customOverlay {
                RestoreWarning(
                    (restoreState as DatabaseRestoreMessage.RequestConfirmation).message,
                    onCancel = {
                        postgresViewModel.cancelRestore()
                        globalState.resetOverlay()
                    },
                    onConfirm = {
                        postgresViewModel.confirmeRestore()
                        globalState.resetOverlay()
                    }
                )
            })
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
fun RestoreWarning(
    command: String, onCancel: () -> Unit, onConfirm: () -> Unit,
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