package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_CONNECTION_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_USERNAME_HINT
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_USERNAME_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_VERSION_LABEL
import io.github.hoeggi.openshiftdb.ui.composables.ExpandableText
import io.github.hoeggi.openshiftdb.ui.composables.StatefulEditTextField

@Composable
internal fun PsqlVersion() {

    val viewModel = ViewModelProvider.current.postgresViewModel
    val version by viewModel.collectAsState(viewModel.version)
    val postgresVersion by viewModel.collectAsState(viewModel.postgresVersion)
    val userName by viewModel.collectAsState(viewModel.userName)

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        ExpandableText(
            text = MessageProvider.message(POSTGRES_VERSION_LABEL)
        ) {
            Spacer(
                modifier = Modifier.height(5.dp)
            )
            Text(
                text = version.psql.trim(),
                style = MaterialTheme.typography.caption
            )
            Spacer(
                modifier = Modifier.height(4.dp)
            )
            Text(
                text = version.pgDump.trim(),
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
                        text = MessageProvider.message(POSTGRES_USERNAME_LABEL),
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = userName,
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
            StatefulEditTextField(
                initialValue = TextFieldValue(userName),
                label = MessageProvider.message(POSTGRES_USERNAME_HINT),
                onValueChange = {
                    viewModel.updateUserName(it.text)
                }
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = MessageProvider.message(POSTGRES_CONNECTION_LABEL),
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "localhost:5432",
                style = MaterialTheme.typography.caption
            )
        }
    }
}
