package io.github.hoeggi.openshiftdb.ui.composables.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ui.composables.outsideClickable
import kotlinx.coroutines.CoroutineScope

@Composable
fun BoxScope.Drawer(
    modifier: Modifier,
    open: Boolean,
    coroutineScope: CoroutineScope,
    onOutsideClick: () -> Unit,
) {

    val updateTransition = updateTransition(targetState = rememberUpdatedState(open))
    val animateColor by updateTransition.animateColor {
        if (it.value) MaterialTheme.colors.background.copy(alpha = 0.7f)
        else MaterialTheme.colors.background.copy(alpha = 0.0f)
    }
    if (open) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animateColor)
                .outsideClickable { onOutsideClick() },
        )
    }
    AnimatedVisibility(
        visible = open,
        enter = slideInHorizontally(
            initialOffsetX = { it }
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it }
        ),
        modifier = Modifier.align(Alignment.CenterEnd)
    ) {
        LocalAppWindow.current.width.dp / 4
        Surface(
            modifier = modifier.align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width((LocalAppWindow.current.width / 4).dp)
                .outsideClickable(),
            elevation = 8.dp
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column {
                    ExportFormatChooser(coroutineScope)
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    ThemeChooser(coroutineScope)
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    FileChooser(coroutineScope)
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LogLevelChooser(coroutineScope)
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    ClusterContext()
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
