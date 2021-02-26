package io.github.hoeggi.openshiftdb

import androidx.compose.foundation.layout.Column
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

@Composable
fun Log(
    modifier: Modifier = Modifier
) {
    val viewModel = GlobalState.current
    val errors by viewModel.syslog.collectAsState(Scope.current.coroutineContext)
    Column(modifier = modifier) {
        Text(
            text = "Syslog",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(10.dp)
        )
        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
        ) {
            items(errors) { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}