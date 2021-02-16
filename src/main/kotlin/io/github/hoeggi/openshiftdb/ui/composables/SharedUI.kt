package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.plus
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent

@Composable
fun EditTextField(
    value: TextFieldValue,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier.shortcuts {
            on(Key.CtrlLeft + Key.V) {
                val text = Toolkit.getDefaultToolkit()
                    .systemClipboard.getData(DataFlavor.stringFlavor)?.toString()
                if (text.isNullOrEmpty().not()) onValueChange(TextFieldValue(text!!))
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
            on(Key.ShiftLeft + Key(KeyEvent.VK_RIGHT)) {
                onValueChange(value.selectNext())
            }
            on(Key.ShiftLeft + Key(KeyEvent.VK_LEFT)) {
                onValueChange(value.selectPrev())
            }
        },
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
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
