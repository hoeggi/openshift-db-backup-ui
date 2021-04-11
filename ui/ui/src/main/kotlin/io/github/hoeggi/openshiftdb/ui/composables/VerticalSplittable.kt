package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.awt.Cursor

internal fun Modifier.cursorForHorizontalResize(onChange: (Boolean) -> Unit): Modifier = composed {
    var isHover by remember { mutableStateOf(false) }

    if (isHover) {
        LocalAppWindow.current.window.cursor = Cursor(Cursor.E_RESIZE_CURSOR)
    } else {
        LocalAppWindow.current.window.cursor = Cursor.getDefaultCursor()
    }

    pointerMoveFilter(
        onEnter = {
            isHover = true
            onChange(isHover)
            true
        },
        onExit = {
            isHover = false
            onChange(isHover)
            true
        }
    )
}

@Composable
internal fun VerticalSplittable(
    modifier: Modifier,
    splitterState: SplitterState,
    onResize: (delta: Dp) -> Unit,
    children: @Composable () -> Unit,
) = Layout(
    {
        children()
        VerticalSplitter(splitterState, onResize)
    },
    modifier,
    measurePolicy = { measurables, constraints ->
        require(measurables.size == 3)

        val firstPlaceable = measurables[0].measure(constraints.copy(minWidth = 0))
        val secondWidth = constraints.maxWidth - firstPlaceable.width
        val secondPlaceable = measurables[1].measure(
            Constraints(
                minWidth = secondWidth,
                maxWidth = secondWidth,
                minHeight = constraints.maxHeight,
                maxHeight = constraints.maxHeight
            )
        )
        val splitterPlaceable = measurables[2].measure(constraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            firstPlaceable.place(0, 0)
            secondPlaceable.place(firstPlaceable.width, 0)
            splitterPlaceable.place(firstPlaceable.width, 0)
        }
    }
)

internal class PanelState(expandedSize: Dp = 300.dp) {
    var expandedSize by mutableStateOf(expandedSize)
    val expandedSizeMin = 90.dp
    val splitter = SplitterState()
}

internal class SplitterState {
    var isResizing by mutableStateOf(false)
    var isResizeEnabled by mutableStateOf(true)
    var isActive by mutableStateOf(false)
}

@Composable
internal fun VerticalSplitter(
    splitterState: SplitterState,
    onResize: (delta: Dp) -> Unit,
    color: Color = MaterialTheme.colors.onSurface,
) = Box {
    val density = LocalDensity.current

    val alpha by animateFloatAsState(
        if (splitterState.isActive) 1f else 0f,
        SpringSpec(stiffness = Spring.StiffnessLow)
    )

    Box(
        Modifier
            .width(8.dp)
            .fillMaxHeight()
            .run {
                if (splitterState.isResizeEnabled) {
                    this.draggable(
                        state = rememberDraggableState {
                            with(density) {
                                onResize(it.toDp())
                            }
                        },
                        orientation = Orientation.Horizontal,
                        startDragImmediately = true,
                        onDragStarted = { splitterState.isResizing = true },
                        onDragStopped = { splitterState.isResizing = false }
                    ).cursorForHorizontalResize { splitterState.isActive = it }
                } else {
                    this
                }
            }
    )

    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color.copy(alpha = alpha))
    )
}
