package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_CONNECT_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_TABLE_LABEL

@Composable
internal fun ConnectButtons() {
    val viewModel = PostgresViewModel.current
    Row(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            onClick = {
                viewModel.postgresVersion()
                viewModel.listLines()
            }
        ) {
            Text(
                text = MessageProvider.message(POSTGRES_CONNECT_LABEL)
            )
        }
        Button(
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            onClick = {
                viewModel.listPretty()
            }
        ) {
            Text(
                text = MessageProvider.message(POSTGRES_TABLE_LABEL)
            )
        }
    }
}