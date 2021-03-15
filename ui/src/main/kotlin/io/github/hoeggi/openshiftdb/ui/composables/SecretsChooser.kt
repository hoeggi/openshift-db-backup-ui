package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.SECRETS_LABEL
import io.github.hoeggi.openshiftdb.outsideClickable

@Composable
internal fun SecretsChooser(modifier: Modifier = Modifier) {
    val viewModel = PostgresViewModel.current
    val secrets by viewModel.collectAsState(viewModel.secrets)
    val updateTransition = updateTransition(targetState = rememberUpdatedState(secrets.isNotEmpty()))
    val animateColor by updateTransition.animateColor {
        if (it.value) MaterialTheme.colors.background.copy(alpha = 0.7f)
        else MaterialTheme.colors.background.copy(alpha = 0.0f)
    }


    if (secrets.isNotEmpty()) {
        Box(
            modifier = modifier.fillMaxSize()
                .background(animateColor)
                .outsideClickable { viewModel.clearSecrets() }
        ) {
            Surface(
                elevation = 8.dp,
                shape = RoundedCornerShape(6.dp),
                modifier = modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.Center)
                    .wrapContentHeight()
                    .padding(16.dp)
                    .outsideClickable(),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 16.dp)) {
                            Text(
                                text = MessageProvider.message(SECRETS_LABEL),
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                            Icon(
                                Icons.Outlined.Close, "",
                                modifier = Modifier.align(Alignment.CenterEnd)
                                    .padding(8.dp)
                                    .clickable {
                                        viewModel.clearSecrets()
                                    }
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(horizontal = 8.dp))
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(secrets) { item ->
                            ExpandableText(
                                text = item.name
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    item.data.forEach {
                                        Column(
                                            modifier = Modifier.clickable {
                                                viewModel.clearSecrets()
                                                viewModel.updatePassword(it.value)
                                            }
                                        ) {
                                            Text(
                                                text = "${it.key}:",
                                                style = MaterialTheme.typography.body2
                                            )
                                            Text(
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
        }
    }
}
