package com.theapache64.ntcdesktop.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theapache64.ntcdesktop.PostgresViewModel

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