package io.github.hoeggi.openshiftdb.ntcdesktop.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.plus
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ntcdesktop.PostgresViewModel
import io.github.hoeggi.openshiftdb.ntcdesktop.Scope
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

@Composable
fun PostgresPassword() {
    val viewModel = PostgresViewModel.current
    val password by viewModel.password.collectAsState(Scope.current.coroutineContext)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(10.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(0.75f)
                .shortcuts {
                    on(Key.CtrlLeft + Key.V) {
                        val text = Toolkit.getDefaultToolkit()
                            .systemClipboard.getData(DataFlavor.stringFlavor)?.toString()
                        if (text.isNullOrEmpty().not()) viewModel.updatePassword(text!!)//onChange(text!!)
                    }
                },
            value = password,
            onValueChange = {
                viewModel.updatePassword(it)
            },
            label = { Text("Password") },
        )
        Button(
            modifier = Modifier.padding(10.dp),
            onClick = {
                viewModel.detectPassword()
            }
        ) {
            Text(
                text = "Detect"
            )
        }
    }
}