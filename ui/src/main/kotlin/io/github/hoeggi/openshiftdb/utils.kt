package io.github.hoeggi.openshiftdb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.github.hoeggi.openshiftdb.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
inline fun <T> BaseViewModel.collectAsState(flow: StateFlow<T>) =
    flow.collectAsState(coroutineScope.coroutineContext)