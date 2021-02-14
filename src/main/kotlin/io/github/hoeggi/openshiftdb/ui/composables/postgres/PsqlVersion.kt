package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope

@Composable
fun PsqlVersion() {

    val viewModel = PostgresViewModel.current
    val psqlVersion by viewModel.psqlVersion.collectAsState(Scope.current.coroutineContext)
    val pgdupmVersion by viewModel.pgdupmVersion.collectAsState(Scope.current.coroutineContext)
    val postgresVersion by viewModel.postgresVersion.collectAsState(Scope.current.coroutineContext)

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Text(
            text = "psql version:",
            style = MaterialTheme.typography.caption
        )
        Text(
            text = psqlVersion,
            style = MaterialTheme.typography.overline
        )
        Text(
            text = "pgdump version:",
            style = MaterialTheme.typography.caption
        )
        Text(
            text = pgdupmVersion,
            style = MaterialTheme.typography.overline
        )
        if(postgresVersion.isNotEmpty()){
            Text(
                text = "postgres version:",
                style = MaterialTheme.typography.caption
            )
            Text(
                text = postgresVersion,
                style = MaterialTheme.typography.overline
            )
            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }

        Row (
            verticalAlignment = Alignment.Bottom
        ){
            Text(
                text = "Username: ",
                style = MaterialTheme.typography.caption
            )
            Text(
                text = "postgres",
                style = MaterialTheme.typography.overline
            )
        }
        Row (
            verticalAlignment = Alignment.Bottom
        ){
            Text(
                text = "Connection: ",
                style = MaterialTheme.typography.caption
            )
            Text(
                text = "localhost:5432",
                style = MaterialTheme.typography.overline
            )
        }
    }

}