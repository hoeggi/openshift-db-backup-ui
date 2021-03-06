package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.desktop.AppManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.GLOBAL_EXIT
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.GLOBAL_OK


@Composable
fun ErrorView(t: Thread?, th: Throwable?, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(
        color = Color.Black.copy(alpha = 0.8f),
    ).clickable { println("box") }) {
        val globalState = GlobalState.current
        Box(
            modifier = Modifier.align(Alignment.Center)
                .fillMaxWidth(0.5f)
                .background(color = MaterialTheme.colors.background, shape = RoundedCornerShape(6.dp))
                .border(2.dp, color = Color.Black, shape = RoundedCornerShape(6.dp))
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    modifier = modifier.padding(4.dp),
                    text = MessageProvider.message(MessageProvider.ERROR_HEADER, th?.message),
                )
                Divider(
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = MessageProvider.message(MessageProvider.ERROR_MESSAGE, t, th?.stackTraceToString()),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.fillMaxHeight(0.15f)
                        .verticalScroll(state = ScrollState(0)).padding(horizontal = 12.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Button(onClick = {
                        globalState.showError(globalState.error())
                    }, modifier = Modifier.weight(1f).padding(6.dp)) {
                        Text(MessageProvider.message(GLOBAL_OK))
                    }
                    Button(onClick = {
                        AppManager.exit()
                    }, modifier = Modifier.weight(1f).padding(6.dp)) {
                        Text(MessageProvider.message(GLOBAL_EXIT))
                    }
                }
            }
        }

    }
}