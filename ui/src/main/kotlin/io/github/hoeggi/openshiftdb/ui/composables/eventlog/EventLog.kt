package io.github.hoeggi.openshiftdb.ui.composables.eventlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.AppMenuControl
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.viewmodel.Argb
import io.github.hoeggi.openshiftdb.viewmodel.ColoredEvent
import io.github.hoeggi.openshiftdb.viewmodel.DatabaseEvent
import io.github.hoeggi.openshiftdb.viewmodel.PortForwardEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


internal fun Argb.toColor() = Color(
    alpha = a,
    red = r,
    green = g,
    blue = b
)

@Composable
internal fun EventLog() {
    val eventsViewModel = ViewModelProvider.current.eventsViewModel
    val globalState = AppMenuControl.current
    rememberCoroutineScope().launch {
        globalState.refreshTrigger.collect {
            eventsViewModel.events()
        }
    }
    eventsViewModel.events()
    EventItems()
}

@Composable
fun EventItems() {
    val eventsViewModel = ViewModelProvider.current.eventsViewModel
    val events by eventsViewModel.collectAsState(eventsViewModel.events)
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(events) {
            Column(modifier = Modifier.padding(4.dp)) {
                with(it.first) {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        color()
                        openIcon()
                        Text("${project}/${service}:${port} $startDate : $startTime",
                            style = MaterialTheme.typography.body2)
                    }
                }
                it.second.forEach {
                    with(it) {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            color()
                            Column(Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                                Row {
                                    eventIcon(Modifier.align(Alignment.Top).size(20.dp))
                                    Text("""
                            $dbname with user $username
                            from: $startDate : $startTime
                            to: $endDate : $endTime
                            ${if (isDownload) "target" else "source"}: $path
                        """.trimIndent(),
                                        style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
                }
                with(it.first) {
                    if (end != null) {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            color()
                            closeIcon()
                            Text("closed on $endDate : $endTime",
                                style = androidx.compose.material.MaterialTheme.typography.body2)
                        }
                    }
                }
            }
            Divider(Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
internal fun ColoredEvent.color() = Box(
    modifier = Modifier.background(color.toColor())
        .width(2.dp)
        .fillMaxHeight(1f)
)

@Composable
internal fun PortForwardEvent.openIcon() = Icon(
    icon = Icons.Outlined.Forward,
    tint = color.toColor()
)

@Composable
internal fun PortForwardEvent.closeIcon() {
    val tintColor = when (isSuccess) {
        true -> color.toColor()
        false -> Color.Red
    }
    val icon = when (isSuccess) {
        true -> Icons.Outlined.Check
        false -> Icons.Outlined.Close
    }
    Icon(
        icon = icon,
        tint = tintColor
    )
}

@Composable
internal fun DatabaseEvent.eventIcon(modifier: Modifier = Modifier) {
    val icon = when (isDownload) {
        true -> Icons.Outlined.Download
        false -> Icons.Outlined.Upload
    }
    val tintColor = when (isSuccess) {
        true -> color.toColor()
        false -> Color.Red
    }
    Icon(
        icon = icon,
        tint = tintColor,
        modifier = modifier
    )
}

@Composable
internal fun DatabaseEvent.resultIcon() = when (isSuccess) {
    true -> Icon(
        icon = Icons.Outlined.Check,
        tint = Color.Green
    )
    false -> Icon(
        icon = Icons.Outlined.Error,
        tint = Color.Red
    )
}

@Composable
internal fun Icon(icon: ImageVector, tint: Color, modifier: Modifier = Modifier) = Icon(
    imageVector = icon,
    tint = tint,
    modifier = modifier.size(24.dp),
    contentDescription = ""
)
