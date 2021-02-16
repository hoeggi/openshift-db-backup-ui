package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.ui.composables.EditTextField

@Composable
fun PostgresPassword() {
    val viewModel = PostgresViewModel.current
    val password by viewModel.password.collectAsState(Scope.current.coroutineContext)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(10.dp)
    ) {
        EditTextField(
            value = password,
            modifier = Modifier.fillMaxWidth(0.75f),
        ) {
            viewModel.updatePassword(it)
        }
        OutlinedButton(
            modifier = Modifier.padding(horizontal = 10.dp),
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