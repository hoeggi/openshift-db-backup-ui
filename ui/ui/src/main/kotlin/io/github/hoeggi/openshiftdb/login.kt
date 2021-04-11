package io.github.hoeggi.openshiftdb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.LOGIN_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.LOGIN_TOKEN_HINT
import io.github.hoeggi.openshiftdb.ui.composables.StatefulEditTextField
import io.github.hoeggi.openshiftdb.ui.composables.navigation.ClusterContext

@Composable
internal fun LoginScreen() {

    val ocViewModel = ViewModelProvider.current.ocViewModel
    val server by ocViewModel.collectAsState(ocViewModel.server)

    var token by remember { mutableStateOf(TextFieldValue("")) }
    var selected by remember { mutableStateOf(-1) }
    ocViewModel.listServer()
    ocViewModel.context()

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
                    label = MessageProvider.message(LOGIN_TOKEN_HINT),
                ) {
                    token = it
                }
                Button(
                    onClick = {
                        ocViewModel.login(token.text, server[selected].server)
                    },
                    enabled = token.text.isNotEmpty() && selected != -1,
                    modifier = Modifier.padding(4.dp),
                ) {
                    Text(text = MessageProvider.message(LOGIN_LABEL))
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
            Column(modifier = Modifier.fillMaxWidth(0.4f)) {
                Spacer(modifier = Modifier.height(10.dp))
                ClusterContext()
            }
        }
    }
}
