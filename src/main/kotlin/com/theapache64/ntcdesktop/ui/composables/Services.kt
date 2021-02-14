package com.theapache64.ntcdesktop.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.theapache64.ntcdesktop.Scope
import com.theapache64.ntcdesktop.process.OC
import com.theapache64.ntcdesktop.process.Service
import kotlinx.coroutines.CoroutineScope

@Composable
fun Service(
    services: OC.OcResult,
    onServiceClicked: (Service, String, CoroutineScope) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Text(
            text = "Available services:",
            style = MaterialTheme.typography.caption
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.25f)
        ) {
            items(when(services){
                is OC.OcResult.Services -> services.services
                else -> listOf()
            }) { item ->
                Service(
                    service = item,
                    onServiceClicked = onServiceClicked
                )
            }
        }
    }
}

@Composable
fun Service(
    service: Service,
    onServiceClicked: (Service, String, CoroutineScope) -> Unit
) {
    Column {
        val scope = Scope.current
        Text(
            text = service.name,
            modifier = Modifier.fillMaxSize().padding(2.dp),
            style = MaterialTheme.typography.overline
        )
        service.ports.forEach {
            Text(text = "* $it",
                modifier = Modifier.fillMaxSize()
                    .padding(2.dp)
                    .clickable {
                        onServiceClicked(service, it, scope)
                    },
                style = MaterialTheme.typography.overline
            )
        }
        Divider(color = Color.White, thickness = 1.dp)
    }
}