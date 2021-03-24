package io.github.hoeggi.openshiftdb.ui.composables.restore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider

@Composable
internal fun User() {

    val viewModel = ViewModelProvider.current.postgresViewModel
    val username by viewModel.collectAsState(viewModel.userName)
    val password by viewModel.collectAsState(viewModel.password)

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Text(
            text = MessageProvider.message(MessageProvider.POSTGRES_USERNAME_HINT),
            style = MaterialTheme.typography.body1
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Text(
            text = username,
            style = MaterialTheme.typography.caption
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Text(
            text = MessageProvider.message(MessageProvider.POSTGRES_PASSWORD_LABEL),
            style = MaterialTheme.typography.body1
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Text(
            text = password,
            style = MaterialTheme.typography.caption
        )
    }
}
