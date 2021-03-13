package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.AppNavigator
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun BottomNav(
    modifier: Modifier = Modifier.height(48.dp),
    coroutineScope: CoroutineScope,
) {
    val viewModel = AppNavigator.current
    val screen by viewModel.screen.collectAsState(coroutineScope.coroutineContext)


    BottomNavigation(
        modifier = modifier
    ) {
        BottomNavigationItem(
            selected = screen == Screen.Main,
            onClick = {
                viewModel.main()
            },
            icon = { Icon(Icons.Outlined.Home, "") }
        )
        BottomNavigationItem(
            selected = screen == Screen.Restore,
            onClick = {
                viewModel.restore()
            },
            icon = { Icon(Icons.Outlined.Restore, "") }
        )
        BottomNavigationItem(
            selected = screen == Screen.Detail,
            onClick = {
                viewModel.detail()
            },
            icon = { Icon(Icons.Outlined.Report, "") }
        )
    }
}