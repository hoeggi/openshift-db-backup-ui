package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope

private var expanded by mutableStateOf(false)

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                expanded = !expanded
            },
        ) {
            Text(
                text = "postgres version data",
                style = MaterialTheme.typography.body1,
            )
            Icon(if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, "")
        }
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        if (expanded) {
            Text(
                text = "psql version:",
                style = MaterialTheme.typography.body1
            )
            Spacer(
                modifier = Modifier.height(5.dp)
            )
            Text(
                text = psqlVersion,
                style = MaterialTheme.typography.caption
            )
            Text(
                text = "pgdump version:",
                style = MaterialTheme.typography.body1
            )
            Spacer(
                modifier = Modifier.height(5.dp)
            )
            Text(
                text = pgdupmVersion,
                style = MaterialTheme.typography.caption
            )
            if (postgresVersion.isNotEmpty()) {
                Text(
                    text = "postgres version:",
                    style = MaterialTheme.typography.body1
                )
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                Text(
                    text = postgresVersion,
                    style = MaterialTheme.typography.caption
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }


        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Username: ",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "postgres",
                style = MaterialTheme.typography.caption
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Connection: ",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "localhost:5432",
                style = MaterialTheme.typography.caption
            )
        }
    }

}