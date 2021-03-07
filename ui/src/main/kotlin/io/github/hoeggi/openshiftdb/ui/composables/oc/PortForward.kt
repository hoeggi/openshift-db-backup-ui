package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.OcViewModel
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_PORTFORWARD_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_PORTFORWARD_PORT
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_PORTFORWARD_PROJECT
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_PORTFORWARD_SERVICE
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_PORTFORWARD_STREAM_LABEL

@Composable
fun PortForward() {

    val viewModel = OcViewModel.current
    val portForwards by viewModel.collectAsState(viewModel.portForward)
    LazyColumn {
        items(portForwards.entries.map {
            it.key to it.value
        }) { it ->
            val target = it.first
            val messages = it.second
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .clickable {
                        viewModel.closePortForward(target)
                    }
            ) {

                Text(
                    text = MessageProvider.message(OC_PORTFORWARD_LABEL),
                    style = MaterialTheme.typography.caption
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                Text(
                    text = MessageProvider.message(OC_PORTFORWARD_PROJECT, target.project),
                    style = MaterialTheme.typography.overline
                )
                Text(
                    text = MessageProvider.message(OC_PORTFORWARD_SERVICE, target.svc),
                    style = MaterialTheme.typography.overline
                )
                Text(
                    text = MessageProvider.message(OC_PORTFORWARD_PORT, "${target.port}", "${target.port}"),
                    style = MaterialTheme.typography.overline
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                Text(
                    text = MessageProvider.message(OC_PORTFORWARD_STREAM_LABEL),
                    style = MaterialTheme.typography.caption,
                )
                messages.forEach {
                    Text(
                        text = it.message,
                        style = MaterialTheme.typography.overline
                    )
                }
                Divider()
            }
        }
    }
}

