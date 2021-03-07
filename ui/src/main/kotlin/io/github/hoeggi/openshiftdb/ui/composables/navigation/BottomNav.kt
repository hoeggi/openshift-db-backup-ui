package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Report
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.GlobalState
import kotlinx.coroutines.CoroutineScope

@Composable
fun BottomNav(
    modifier: Modifier = Modifier.height(48.dp),
    coroutineScope: CoroutineScope,
) {
    val viewModel = GlobalState.current
    val screen by viewModel.screen.collectAsState(coroutineScope.coroutineContext)


    BottomNavigation(
        modifier = modifier
    ) {
        BottomNavigationItem(
            modifier = Modifier.fillMaxWidth(),
            selected = screen == Screen.Main,
            onClick = {
                viewModel.main()
            },
            icon = { Icon(Icons.Outlined.Home, "") }
        )
        BottomNavigationItem(
            modifier = Modifier.fillMaxWidth(),
            selected = screen == Screen.Detail,
            onClick = {
                viewModel.detail()
            },
            icon = { Icon(Icons.Outlined.Report, "") }
        )
    }
}