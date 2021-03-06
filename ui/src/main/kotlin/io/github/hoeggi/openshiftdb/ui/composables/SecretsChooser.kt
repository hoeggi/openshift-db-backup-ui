package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.viewmodel.PostgresViewModel

@Composable
fun SecretsChooser(modifier: Modifier = Modifier, viewModel: PostgresViewModel) {

    val secrets by viewModel.collectAsState(viewModel.secrets)

    if (secrets.isNotEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.75f)
                .padding(16.dp)
                .background(color = MaterialTheme.colors.background, shape = RoundedCornerShape(6.dp))
                .border(1.dp, color = MaterialTheme.colors.onBackground, shape = RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.TopStart,

            ) {
            Icon(
                Icons.Outlined.Close, "",
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable {
                        viewModel.clearSecrets()
                    }
            )
            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                items(secrets) { item ->
                    ExpandableText(
                        text = item.name
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                        ) {
                            item.data.forEach {
                                Text(
                                    text = "${it.key}:",
                                    style = MaterialTheme.typography.body2
                                )
                                Text(
                                    modifier = Modifier.clickable {
                                        viewModel.clearSecrets()
                                        viewModel.updatePassword(it.value)
                                    },
                                    text = "${it.value}",
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
