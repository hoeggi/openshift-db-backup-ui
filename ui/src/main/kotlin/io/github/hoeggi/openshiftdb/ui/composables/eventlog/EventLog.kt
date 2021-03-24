package io.github.hoeggi.openshiftdb.ui.composables.eventlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.ui.composables.navigation.MenuControlProvider
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
    val globalState = MenuControlProvider()
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
                PortForwardOpen(it.first)
                DatabaseEvents(it.second)
                PortForwardClose(it.first)
            }
        }
    }
}

@Composable
internal fun DatabaseEvents(databaseEvents: List<DatabaseEvent>) = databaseEvents.forEach {
    with(it) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            color()
            Column(Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                Row {
                    eventIcon(Modifier.align(Alignment.Top).size(20.dp))
                    Text(
                        """
                            $dbname with user $username
                            from: $startDate : $startTime
                            to: $endDate : $endTime
                            ${if (isDownload) "target" else "source"}: $path
                        """.trimIndent(),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}

@Composable
internal fun PortForwardClose(portForwardEvent: PortForwardEvent) = with(portForwardEvent) {
    if (isFinished) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            color()
            closeIcon()
            Text(
                "closed $endDate, $endTime",
                style = MaterialTheme.typography.body2
            )
            if (!isSuccess) {
                Icon(
                    Icons.Outlined.Close,
                    Color.Red,
                    Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun PortForwardOpen(portForwardEvent: PortForwardEvent) = with(portForwardEvent) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        color()
        openIcon()
        Text(
            "$project/$service:$port $startDate, $startTime",
            style = MaterialTheme.typography.body2
        )
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
    icon = Icons.Outlined.ChevronRight,
    tint = color.toColor()
)

@Composable
internal fun PortForwardEvent.closeIcon() {
    Icon(
        icon = Icons.Outlined.ChevronLeft,
        tint = color.toColor()
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
internal fun Icon(icon: ImageVector, tint: Color, modifier: Modifier = Modifier) = Icon(
    imageVector = icon,
    tint = tint,
    modifier = modifier.size(24.dp),
    contentDescription = ""
)
