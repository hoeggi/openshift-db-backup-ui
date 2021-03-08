package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.outsideClickable
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import io.github.hoeggi.openshiftdb.ui.composables.oc.CurrentProject
import io.github.hoeggi.openshiftdb.ui.composables.oc.PortForward
import io.github.hoeggi.openshiftdb.ui.theme.customOverlay

@Composable
fun RestoreView() {
    val globalState = GlobalState.current
    val postgresViewModel = PostgresViewModel.current
    val path by postgresViewModel.collectAsState(postgresViewModel.restorePath)
    val command by postgresViewModel.collectAsState(postgresViewModel.restoreCommand)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 48.dp),
    ) {
        BackupPicker()
        Divider()
        if (!path.isNullOrBlank()) {
            Row {
                Column(Modifier.fillMaxWidth(0.7f)) {
                    RestoreData()
                    Divider()
                    Button(
                        modifier = Modifier.padding(10.dp),
                        onClick = {
                            globalState.showOverlay(customOverlay {
                                RestoreWarning(globalState, command.command)
                            })
                        }
                    ) {
                        Text("Restore Database")
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

@Composable
fun RestoreWarning(globalState: GlobalState, command: String) {
    Box(
        modifier = Modifier.background(MaterialTheme.colors.background.copy(alpha = 0.7f))
            .outsideClickable { globalState.resetOverlay() }
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
                        onClick = {
//                            viewModel.postgresVersion()
//                            viewModel.listLines()
                        }
                    ) {
                        Icon(Icons.Outlined.FlashOn, "", modifier = Modifier.size(16.dp))
                        Text(text = "Restore")
                        Icon(Icons.Outlined.FlashOn, "", modifier = Modifier.size(16.dp))
                    }
                    Button(
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        onClick = {
                            globalState.resetOverlay()
                        }
                    ) {
                        Text(text = "Close")
                    }
                }
            }

        }
    }
}