package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.OcViewModel
import io.github.hoeggi.openshiftdb.Scope

@Composable
fun PortForward() {

    val viewModel = OcViewModel.current
    val portForwards by viewModel.portForward.collectAsState(Scope.current.coroutineContext)

    LazyColumn {
        items(portForwards.entries.map {
            it.key to it.value
        }) {
            val target = it.first
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .clickable {
                        viewModel.closePortForward(it.first)
                    }
            ) {

                Text(
                    text = "Forwarding to:",
                    style = MaterialTheme.typography.caption
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                Text(
                    text = "Project: ${target.project}",
                    style = MaterialTheme.typography.overline
                )
                Text(
                    text = "Service: ${target.svc}",
                    style = MaterialTheme.typography.overline
                )
                Text(
                    text = "Port: ${target.port}:${target.port}",
                    style = MaterialTheme.typography.overline
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                Text(
                    text = "Stream: ",
                    style = MaterialTheme.typography.caption,
                )
                val lines = it.second
                LazyColumn {
                    items(lines) { item ->
                        Text(
                            text = item.message,
                            style = MaterialTheme.typography.overline
                        )
                    }
                }
            }
        }
    }
}