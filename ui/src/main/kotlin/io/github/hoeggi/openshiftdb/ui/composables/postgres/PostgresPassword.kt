package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_PASSWORD_DETECT
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.POSTGRES_PASSWORD_LABEL
import io.github.hoeggi.openshiftdb.ui.composables.EditTextField

@Composable
internal fun PostgresPassword() {
    val viewModel = PostgresViewModel.current
    val password by viewModel.collectAsState(viewModel.password)
    var internalPassword by remember { mutableStateOf(TextFieldValue("")) }
    internalPassword = internalPassword.copy(
        text = password
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(10.dp)
    ) {
        EditTextField(
            value = internalPassword,
            modifier = Modifier.fillMaxWidth(0.7f),
            label = MessageProvider.message(POSTGRES_PASSWORD_LABEL)
        ) {
            internalPassword = it
            viewModel.updatePassword(it.text)
        }
        Button(
            modifier = Modifier.padding(horizontal = 10.dp),
            onClick = {
                viewModel.detectPassword()
            }
        ) {
            Text(text = MessageProvider.message(POSTGRES_PASSWORD_DETECT))
        }
        IconButton(
            onClick = {
                viewModel.secrets()
            },
        ) {
            Icon(Icons.Outlined.List, contentDescription = "")
        }
    }
}