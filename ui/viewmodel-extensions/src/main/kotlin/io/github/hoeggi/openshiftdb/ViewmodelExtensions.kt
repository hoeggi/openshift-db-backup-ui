package io.github.hoeggi.openshiftdb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.hoeggi.openshiftdb.viewmodel.BaseViewModel
import io.github.hoeggi.openshiftdb.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.StateFlow

@Composable
inline fun <T> BaseViewModel.collectAsState(flow: StateFlow<T>) =
    flow.collectAsState(coroutineScope.coroutineContext)

val ViewModelProvider = staticCompositionLocalOf<ViewModelFactory> {
    error("unexpected call to ViewModelProvider")
}

// object ViewModelProvider {
//    lateinit var current: ViewModelFactory
// }
