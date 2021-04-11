package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.ui.graphics.Color

internal object ColorMapping {
    val colors = mapOf(
//        TextColor.BLACK to Color(0, 0, 0),
        "ERROR" to Color(219, 84, 81), // RED
//        TextColor.GREEN to Color(84, 140, 38),
        "WARN" to Color(168, 144, 34), // YELLOW
        "INFO" to Color(58, 145, 207), // BLUE
//        TextColor.MAGENTA to Color(165, 117, 186),
//        TextColor.CYAN to Color(0, 145, 145),
        "DEBUG" to Color(191, 191, 191), // LIGHT GREY
        "TRACE" to Color.White
    )
}
