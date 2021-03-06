package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.ext.DesktopApi
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_DUMP_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_DUMP_LOADING
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_DUMP_SUCCESS
import io.github.hoeggi.openshiftdb.ui.composables.launchInIo
import org.slf4j.LoggerFactory
import java.io.File

@Composable
fun PostgresDump() {

    val logger = LoggerFactory.getLogger("io.github.hoeggi.openshiftdb.ui.composables.postgres.PostgresDump")

    val viewModel = PostgresViewModel.current
    val globalState = GlobalState.current
    val scope = Scope.current
    val selectedDatabase by viewModel.selectedDatabase.collectAsState(-1, scope.coroutineContext)
    val resultState by viewModel.downloadState.collectAsState(scope.coroutineContext)
    val databases by viewModel.databasesLines.collectAsState(listOf(), scope.coroutineContext)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            launchInIo(scope) {
                viewModel.dumpDatabase(databases[selectedDatabase], globalState.exportFormat.value.format)
            }
        }) {
            Text(text = MessageProvider.message(POSTGRES_DUMP_LABEL))
        }
        when (resultState) {
            is DatabaseDownloadMessage.FinishMessage -> {
                val path = resultState.message
                Text(
                    text = MessageProvider.message(POSTGRES_DUMP_SUCCESS, path),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(10.dp).clickable {
                        val open = DesktopApi.open(File(path).parentFile)
                        logger.debug("openend: $open")
                    },
                )
            }
            is DatabaseDownloadMessage.ErrorMessage -> {
                Text(
                    text = "${resultState.message}",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(10.dp)
                )
            }
            is DatabaseDownloadMessage.InProgressMessage,
            is DatabaseDownloadMessage.StartMessage -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(10.dp))
                    Text(
                        text = MessageProvider.message(POSTGRES_DUMP_LOADING),
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

