package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.plus
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

@Composable
internal fun Loading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(10.dp))
    }
}

@Composable
internal fun ExpandableText(
    initialState: Boolean = false,
    header: @Composable (RowScope.(Boolean) -> Unit),
    content: @Composable (() -> Unit),
) {
    val expanded = remember { mutableStateOf(initialState) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            expanded.value = !expanded.value
        },
    ) {
        header(expanded.value)
    }
    if (expanded.value) {
        content()
    }
}

@Composable
internal fun ExpandableText(
    text: String,
    style: TextStyle = MaterialTheme.typography.body1,
    initialState: Boolean = false,
    content: @Composable (() -> Unit),
) {
    ExpandableText(
        initialState = initialState,
        header = { expanded ->
            Crossfade(targetState = expanded) {
                if (it) Icon(Icons.Outlined.KeyboardArrowUp, "")
                else Icon(Icons.Outlined.KeyboardArrowDown, "")
            }
            Text(
                text = text,
                style = style,
            )
        },
        content = content,
    )
}

//  https://github.com/JetBrains/compose-jb/issues/220
//            .combinedClickable(
//                onDoubleClick = {
//                    println("DOUBLE CLICK")
//                    _onValueChange(
//                        value.copy(selection = TextRange(0, value.text.length))
//                    )
//                },
//                onClick = {
//                    println("CLICK")
//                }
//            )

@Composable
internal fun EditTextField(
    value: TextFieldValue,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier
            .shortcuts {
                on(Key.CtrlLeft + Key.V) {
                    val text = Toolkit.getDefaultToolkit()
                        .systemClipboard.getData(DataFlavor.stringFlavor)?.toString()
                    if (text.isNullOrEmpty().not()) {
                        if (value.selection.collapsed) {
                            val prefix = value.text.substring(0, value.selection.start)
                            val postfix = value.text.substring(value.selection.start)
                            onValueChange(
                                value.copy(
                                    text = "$prefix$text$postfix",
                                    selection = TextRange(value.selection.start + text!!.length)
                                )
                            )
                        } else {
                            val selected = value.selection
                            val prefix = value.text.substring(0, selected.start.coerceAtMost(selected.end))
                            val postfix = value.text.substring(selected.start.coerceAtLeast(selected.end))
                            onValueChange(
                                value.copy(
                                    text = "$prefix$text$postfix",
                                    selection = TextRange(selected.start.coerceAtMost(selected.end) + text!!.length)
                                )
                            )
                        }
                    }
                }
                on(Key.CtrlLeft + Key.C) {
                    val selected = value.selection
                    val text = value.text.substring(
                        selected.start.coerceAtMost(selected.end),
                        selected.start.coerceAtLeast(selected.end)
                    )
                    if (text.isNotEmpty()) Toolkit.getDefaultToolkit()
                        .systemClipboard.setContents(StringSelection(text), null)
                }
                on(Key.Delete) {
                    val start = value.selection.start
                    val end = value.selection.start
                    val length = value.text.length
                    if (start <= end && start < length && end < length) {
                        onValueChange(
                            value.copy(
                                text = value.text.removeRange(start..end)
                            )
                        )
                    }
                }
                on(Key.Home) {
                    onValueChange(value.moveCursor(0))
                }
                on(Key(KeyEvent.VK_END)) {
                    onValueChange(value.moveCursor(value.text.length))
                }
                on(Key.ShiftLeft + Key.Home) {
                    onValueChange(
                        value.copy(
                            selection = TextRange(value.selection.start, 0)
                        )
                    )
                }
                on(Key.ShiftLeft + Key(KeyEvent.VK_END)) {
                    onValueChange(
                        value.copy(
                            selection = TextRange(value.selection.start, value.text.length)
                        )
                    )
                }
                on(Key.ShiftLeft + Key(KeyEvent.VK_RIGHT)) {
                    onValueChange(value.selectNext())
                }
                on(Key.ShiftLeft + Key(KeyEvent.VK_LEFT)) {
                    onValueChange(value.selectPrev())
                }
            },
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
    )
}

@Composable
internal fun StatefulEditTextField(
    initialValue: TextFieldValue,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
) {
    var value by remember { mutableStateOf(initialValue) }
    val _onValueChange: (TextFieldValue) -> Unit = {
        value = it
        onValueChange(it)
    }
    EditTextField(
        value = value,
        label = label,
        modifier = modifier,
        onValueChange = _onValueChange
    )
}

private fun TextFieldValue.moveCursor(position: Int) = copy(
    selection = TextRange(position)
)

private fun TextFieldValue.selectNext() = copy(
    selection = TextRange(selection.start, selection.end + 1)
)

private fun TextFieldValue.selectPrev() = copy(
    selection = TextRange(selection.start, selection.left())
)

private fun TextRange.left(): Int = if (end - 1 < 0) 0 else end - 1
