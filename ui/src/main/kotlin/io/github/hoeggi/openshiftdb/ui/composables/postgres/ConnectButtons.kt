package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.ui.composables.launchInIo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ConnectButtons() {
    val viewModel = PostgresViewModel.current
    val scope = Scope.current

    Row(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            onClick = {
                launchInIo(scope) {
                    viewModel.postgresVersion()
                    viewModel.listLines()
                }
            }
        ) {
            Text(
                text = "Connect"
            )
        }
        Button(
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            onClick = {
                launchInIo(scope) {
                    viewModel.listPretty()
                }
            }
        ) {
            Text(
                text = "List Databases Tabel"
            )
        }
    }
}