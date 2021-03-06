package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import io.github.hoeggi.openshiftdb.api.response.ServicesApi
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_SERVICE_AVAILABLE

@Composable
fun Service() {

    val viewModel = OcViewModel.current
    val services by viewModel.collectAsState(viewModel.services)
    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Text(
            text = MessageProvider.message(OC_SERVICE_AVAILABLE),
            style = MaterialTheme.typography.body1
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.25f)
        ) {
            items(services) { item ->
                Service(
                    service = item,
                    onServiceClicked = { service, port ->
                        viewModel.portForward(service.name, port)
                    }
                )
            }
        }
    }
}

@Composable
fun Service(
    service: ServicesApi,
    onServiceClicked: (ServicesApi, Int) -> Unit,
) {
    Column {
        Text(
            text = service.name,
            modifier = Modifier.fillMaxSize().padding(2.dp),
            style = MaterialTheme.typography.caption
        )
        service.ports.forEach {
            Text(
                text = "* ${it.port}:${it.targetPort}/${it.protocol}",
                modifier = Modifier.fillMaxSize()
                    .padding(2.dp)
                    .clickable {
                        onServiceClicked(service, it.port)
                    },
                style = MaterialTheme.typography.caption
            )
        }
        Divider(modifier = Modifier.padding(vertical = 1.dp))
    }
}