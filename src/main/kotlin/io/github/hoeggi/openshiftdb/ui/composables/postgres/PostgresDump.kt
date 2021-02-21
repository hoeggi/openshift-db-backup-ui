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
import io.github.hoeggi.openshiftdb.DesktopApi
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.process.Postgres
import java.io.File


@Composable
fun PostgresDump() {

    val viewModel = PostgresViewModel.current

    val selectedDatabase by viewModel.selectedDatabase.collectAsState(-1, Scope.current.coroutineContext)
    val resultState by viewModel.downloadState.collectAsState(
        Scope.current.coroutineContext
    )
    val databases by viewModel.databasesLines.collectAsState(listOf(), Scope.current.coroutineContext)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            viewModel.dumpDatabase(databases[selectedDatabase])
        }) {
            Text(text = "Dump")
        }
        when (resultState) {
            is Postgres.PostgresResult.Download.Success -> {
                val path = (resultState as Postgres.PostgresResult.Download.Success).path
                Text(
                    text = "Success: $path",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(10.dp).clickable {
                        val open = DesktopApi.open(File(path).parentFile)
                        println("opened: $open")
                    },
                )
            }
            is Postgres.PostgresResult.Download.Error -> {
                val error = (resultState as Postgres.PostgresResult.Download.Error)
                Text(
                    text = "Error: exited: ${error.exitCode} ex: ${error.ex?.message}",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(10.dp)
                )
            }
            is Postgres.PostgresResult.Download.InProgres,
            Postgres.PostgresResult.Download.Started -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(10.dp))
                    Text(
                        text = "Waiting...",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

