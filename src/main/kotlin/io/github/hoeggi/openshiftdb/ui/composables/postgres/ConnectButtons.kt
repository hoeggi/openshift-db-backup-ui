package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel

@Composable
fun ConnectButtons(){
    val viewModel = PostgresViewModel.current
    Row(
        modifier = Modifier
            .padding(10.dp)
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            onClick = {
                viewModel.postgresVersion()
                viewModel.listLines()
//                viewModel.selectDefaultDatabase()
            }
        ) {
            Text(
                text = "Connect"
            )
        }
        OutlinedButton(
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            onClick = {
                viewModel.listPretty()
            }
        ) {
            Text(
                text = "List Databases Tabel"
            )
        }
    }
}