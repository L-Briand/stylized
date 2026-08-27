package net.orandja.kt.stylized.examples

import net.orandja.kt.stylized.attr
import net.orandja.kt.stylized.plus
import net.orandja.kt.stylized.ref
import net.orandja.kt.stylized.style
import kotlin.test.Test

class ColorTheming {
    enum class Color { RED, GREEN, BLUE }

    val red by attr<Color?>(null)
    val green by attr<Color>()
    val blue by attr<Color>()
    val colorPrimary by attr(red)
    val colorSecondary by attr<Color>()

    val palette by style {
        red by Color.RED
        green by Color.GREEN
        blue by Color.BLUE
    }

    val mergedStyle by style(palette) {
        colorPrimary by red
        colorSecondary by ref("blue")
    }

    val addedStyle by style {
        +palette
        colorPrimary by palette + red
        colorSecondary by attr("palette.green")
    }

    @Test
    fun test() {
        println(mergedStyle)
        println(addedStyle)
    }
}