package io.github.hoeggi.openshiftdb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.api.response.ClusterApi
import io.github.hoeggi.openshiftdb.ui.composables.StatefulEditTextField
import io.github.hoeggi.openshiftdb.ui.composables.launchInIo
import io.github.hoeggi.openshiftdb.viewmodel.OcViewModel
import androidx.compose.runtime.getValue

@Composable
fun LoginScreen(ocViewModel: OcViewModel) {

    val scope = Scope.current
    val server: List<ClusterApi> by ocViewModel.server.collectAsState(scope.coroutineContext)

    var token by remember { mutableStateOf(TextFieldValue("")) }
    var selected by remember { mutableStateOf(-1) }
    launchInIo(scope) {
        ocViewModel.listServer()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
    ) {
        Column(
            modifier = Modifier.wrapContentSize(
                align = Alignment.Center
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatefulEditTextField(
                    initialValue = TextFieldValue(""),
                    label = "Token",
                ) {
                    token = it
                }
                Button(
                    onClick = {
                        launchInIo(scope) {
                            ocViewModel.login(token.text, server[selected].server)
                        }
                    },
                    enabled = token.text.isNotEmpty() && selected != -1,
                    modifier = Modifier.padding(4.dp),
                ) {
                    Text(text = "Login")
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                items(server.size) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            modifier = Modifier.padding(4.dp),
                            selected = it == selected,
                            onClick = {
                                selected = it
                            },
                        )
                        Text(
                            modifier = Modifier.clickable {
                                selected = it
                            },
                            text = server[it].server,
                            style = MaterialTheme.typography.body2,
                        )
                    }
                }
            }
        }
    }
}