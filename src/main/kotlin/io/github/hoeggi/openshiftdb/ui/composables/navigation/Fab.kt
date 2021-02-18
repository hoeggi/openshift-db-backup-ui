package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.hoeggi.openshiftdb.GlobalState
import io.github.hoeggi.openshiftdb.Scope

@Composable
fun Fab(
    modifier: Modifier = Modifier
) {
    val viewModel = GlobalState.current
    val dark by viewModel.theme.collectAsState(Scope.current.coroutineContext)

    FloatingActionButton(
        onClick = {
            viewModel.toggleTheme()
        },
        modifier = modifier//Modifier.align(Alignment.BottomEnd).padding(bottom = 15.dp, end = 15.dp),
    ) {
        Icon(
            when (dark) {
                Theme.Dark -> Icons.Outlined.ModeNight
                Theme.Light -> Icons.Filled.ModeNight
            }, ""
        )
    }
}