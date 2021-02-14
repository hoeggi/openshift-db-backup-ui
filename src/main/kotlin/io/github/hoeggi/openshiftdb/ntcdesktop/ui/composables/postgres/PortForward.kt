package io.github.hoeggi.openshiftdb.ntcdesktop.ui.composables

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ntcdesktop.BACKGROUND
import io.github.hoeggi.openshiftdb.ntcdesktop.Scope
import io.github.hoeggi.openshiftdb.ntcdesktop.process.OC
import kotlinx.coroutines.*
import java.io.IOException

@Composable
fun PortForward(
    target: OC.PortForward,
    onClick: (OC.PortForward) -> Unit
) {

    var lines by mutableStateOf(listOf<String>())
    val scope = Scope.current
    scope.launch {

        async(Dispatchers.BACKGROUND) {
            var streamOpen = true
            while (target.isAlive && streamOpen) {
                try {
                    val line = target.stream.readLine()
                    withContext(Dispatchers.Main) {
                        lines = if (line != null) lines.toMutableList().apply {
                            add(line)
                        } else lines
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    streamOpen = false
                }
            }
        }

        async(Dispatchers.BACKGROUND) {
            var streamOpen = true
            while (target.isAlive && streamOpen) {
                try {
                    val line = target.errorStream.readLine()
                    withContext(Dispatchers.Main) {
                        lines = if (line != null) lines.toMutableList().apply {
                            add(line)
                        } else lines
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    streamOpen = false
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .padding(10.dp)
            .clickable {
                onClick(target)
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
            text = "Project: ${target.target.projectName}",
            style = MaterialTheme.typography.overline
        )
        Text(
            text = "Service: ${target.target.serviceName}",
            style = MaterialTheme.typography.overline
        )
        Text(
            text = "Port: ${target.target.port}:${target.target.port}",
            style = MaterialTheme.typography.overline
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Text(
            text = "Stream: ",
            style = MaterialTheme.typography.caption,
        )
        LazyColumn {
            items(lines) { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.overline
                )
            }
        }
    }
}