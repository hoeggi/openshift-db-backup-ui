package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.ui.composables.EditTextField
import io.github.hoeggi.openshiftdb.ui.composables.ExpandableText

@Composable
fun PsqlVersion() {

    val viewModel = PostgresViewModel.current
    val psqlVersion by viewModel.psqlVersion.collectAsState(Scope.current.coroutineContext)
    val pgdupmVersion by viewModel.pgdupmVersion.collectAsState(Scope.current.coroutineContext)
    val postgresVersion by viewModel.postgresVersion.collectAsState(Scope.current.coroutineContext)
    val userName by viewModel.userName.collectAsState(Scope.current.coroutineContext)

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        ExpandableText(
            text = "postgres version data"
        ) {
            Spacer(
                modifier = Modifier.height(5.dp)
            )
            Text(
                text = psqlVersion.trim(),
                style = MaterialTheme.typography.caption
            )
            Spacer(
                modifier = Modifier.height(4.dp)
            )
            Text(
                text = pgdupmVersion.trim(),
                style = MaterialTheme.typography.caption
            )
            if (postgresVersion.isNotEmpty()) {
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
                Text(
                    text = postgresVersion.trim(),
                    style = MaterialTheme.typography.caption
                )
            }
        }
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        ExpandableText(
            initialState = false,
            header = {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Username: ",
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = userName.text,
                        style = MaterialTheme.typography.caption,
                    )
                    Icon(
                        Icons.Default.Edit,
                        "",
                        modifier = Modifier.padding(horizontal = 4.dp).size(16.dp)
                    )
                }
            }
        ) {
            EditTextField(
                value = userName,
                label = "Username",
                onValueChange = {
                    viewModel.updateUserName(it)
                }
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