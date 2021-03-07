package io.github.hoeggi.openshiftdb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.hoeggi.openshiftdb.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
inline fun <T> BaseViewModel.collectAsState(flow: StateFlow<T>) =
    flow.collectAsState(coroutineScope.coroutineContext)

@Composable
inline fun Modifier.outsideClickable(crossinline onClick: () -> Unit = {}) = clickable(
    indication = null,
    interactionSource = remember { MutableInteractionSource() },
) { onClick() }