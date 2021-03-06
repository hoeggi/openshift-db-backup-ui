package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import net.rubygrapefruit.ansi.TextColor

object ColorMapping {
    val colors = mapOf(
        TextColor.BLACK to Color(0, 0, 0),
        TextColor.RED to Color(219, 84, 81),
        TextColor.GREEN to Color(84, 140, 38),
        TextColor.YELLOW to Color(168, 144, 34),
        TextColor.BLUE to Color(58, 145, 207),
        TextColor.MAGENTA to Color(165, 117, 186),
        TextColor.CYAN to Color(0, 145, 145),
        TextColor.WHITE to Color(191, 191, 191),
    )
}