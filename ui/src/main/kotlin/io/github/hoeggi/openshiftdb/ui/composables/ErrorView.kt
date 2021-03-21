package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.desktop.AppManager
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.GLOBAL_EXIT
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.GLOBAL_OK
import io.github.hoeggi.openshiftdb.outsideClickable
import io.github.hoeggi.openshiftdb.ui.composables.navigation.AppErrorViewer

@Composable
internal fun ErrorView(t: Thread?, th: Throwable?) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            color = MaterialTheme.colors.background.copy(alpha = 0.7f),
        ).outsideClickable()
    ) {
        val errorViewer = AppErrorViewer()
        Surface(
            elevation = 8.dp,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.align(Alignment.Center)
                .fillMaxWidth(0.5f)
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    modifier = Modifier.padding(4.dp),
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
                        errorViewer.showError(errorViewer.empty())
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