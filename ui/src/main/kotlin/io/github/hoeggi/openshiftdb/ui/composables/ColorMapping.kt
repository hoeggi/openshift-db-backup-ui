package io.github.hoeggi.openshiftdb.ui.composables

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import net.rubygrapefruit.ansi.TextColor

class ColorMapping {

//   val black = TextColor.BLACK to Color(0,0,0)
//   val red = TextColor.RED to Color(153,0,0)
//   val green = TextColor.GREEN to Color(0,166,0)
//   val yellow = TextColor.YELLOW to Color(153,153,0)
//   val blue = TextColor.BLUE to Color(0,0,178)
//   val magenta = TextColor.MAGENTA to Color(178,0,178)
//   val cyan = TextColor.CYAN to Color(0,166,178)
//   val white = TextColor.WHITE to Color(191,191,191)

    companion object {
        val colors = mapOf(
            TextColor.BLACK to Color(0, 0, 0),
            TextColor.RED to Color(219,84,81),
            TextColor.GREEN to Color(84, 140, 38),
            TextColor.YELLOW to Color(168,144,34),
            TextColor.BLUE to Color(58, 145, 207),
            TextColor.MAGENTA to Color(165,117,186),
            TextColor.CYAN to Color(0, 145, 145),
            TextColor.WHITE to Color(191, 191, 191),
        )

        val colorsBright = mapOf(
            TextColor.BLACK to Color(102,102,102),
            TextColor.RED to Color(229,0,0),
            TextColor.GREEN to Color(0,217,0),
            TextColor.YELLOW to Color(229,229,0),
            TextColor.BLUE to Color(0,0,255),
            TextColor.MAGENTA to Color(229,0,229),
            TextColor.CYAN to Color(0,229,229),
            TextColor.WHITE to Color(229,229,229),
        )

        val colorsMaterial = mapOf(
            TextColor.BLACK to Color.Black,
            TextColor.RED to Color.Red,
            TextColor.GREEN to Color.Green,
            TextColor.YELLOW to Color.Yellow,
            TextColor.BLUE to Color.Blue,
            TextColor.MAGENTA to Color.Magenta,
            TextColor.CYAN to Color.Cyan,
            TextColor.WHITE to Color.White,
        )
    }

}