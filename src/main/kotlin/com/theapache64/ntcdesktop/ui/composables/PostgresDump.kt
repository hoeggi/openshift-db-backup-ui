package com.theapache64.ntcdesktop.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.theapache64.ntcdesktop.DesktopApi
import com.theapache64.ntcdesktop.PostgresViewModel
import com.theapache64.ntcdesktop.Scope
import com.theapache64.ntcdesktop.process.Postgres
import kotlinx.coroutines.flow.filterIsInstance
import java.awt.Desktop
import java.io.File
import java.lang.Exception
import java.net.URI


@Composable
fun PostgresDump() {

    val viewModel = PostgresViewModel.current

    val selectedDatabase by viewModel.selectedDatabase.collectAsState(-1, Scope.current.coroutineContext)
    val resultState by viewModel.downloadState.collectAsState(
        Postgres.PostgresResult.Download.Unspecified,
        Scope.current.coroutineContext
    )
    val databases by viewModel.databasesLines.collectAsState(listOf(), Scope.current.coroutineContext)
    val downloadState: Postgres.PostgresResult.Download by viewModel.downloadState
        .filterIsInstance<Postgres.PostgresResult.Download.InProgres>()
        .collectAsState(
            Postgres.PostgresResult.Download.Unspecified,
            Scope.current.coroutineContext
        )

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
                    text = "Error: exited: ${error.ex} ex: ${error?.ex?.message}",
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

    when (downloadState) {
        is Postgres.PostgresResult.Download.InProgres -> {
            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                LazyColumn {
                    val lines = (downloadState as Postgres.PostgresResult.Download.InProgres).lines
                    items(lines) { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.overline
                        )
                    }
                }
            }
        }
    }

}

