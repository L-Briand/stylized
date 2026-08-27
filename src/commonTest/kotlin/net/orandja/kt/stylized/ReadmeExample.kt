package net.orandja.kt.stylized

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadmeExample {
    enum class Color { RED, GREEN, BLUE, WHITE }

    @Test
    fun readme() {
        val white by attr() // Simple attribute. Reference itself. (Same as: attr { it["white"] })
        val blue by attr(Color.BLUE) // Reference an object. It can be anything.
        val colorPrimary by attr(blue) // Reference a reference.
        val colorBackground by attr { it["green"] } // Reference an unknown attribute.

        // You can 'get' the value of an attribute
        val blueColor = blue.get<Color>()
        assertEquals(Color.BLUE, blueColor)
        assertEquals(blueColor, colorPrimary.get())


        val colors by style {
            +blue // Attributes can be added to a style.
            white by Color.WHITE // Attributes can be used as keys.
            "green" by Color.GREEN // You can declare a value in a style without attribute.
            "example" by blue // Attributes can be used as values.
        }

        // You can then 'get' the value of a style with a path.
        val _colorWhite = colors.get<Color>("white")
        val colorWhite: Color = colors[white] // Or use the get, "[]" operator if the type is known

        println(colors)
        assertEquals(Color.WHITE, colorWhite)
        assertEquals(Color.BLUE, colors[blue])
        assertEquals(Color.GREEN, colors["green"])
        assertEquals(Color.BLUE, colors["example"])

        val green by attr()
        assertEquals(Color.GREEN, colors[green])


        val lightTheme by style(from = colors) {
            // Even if 'colorPrimary' has a value, we can redefine it.
            colorPrimary by ref("green")

            // 'white' does not have a default value, but it references to itself.
            // 'colors' contains 'white', so it will find it.
            colorBackground by white

            // just so you know:
            "example1" by colorPrimary // reference colorPrimary, which is green
            "example2" by colorPrimary.dereference() // reference the value of the attribute of colorPrimary, which is blue
        }

        assertEquals(blueColor, colorPrimary.get()) // 'colorPrimary' still is blue
        assertEquals(Color.GREEN, lightTheme[colorPrimary]) // but 'colorPrimary' in lightTheme is green
        assertEquals(Color.GREEN, lightTheme[green])
        assertEquals(Color.WHITE, lightTheme[colorBackground])
        assertEquals(Color.GREEN, lightTheme["example1"])
        assertEquals(Color.BLUE, lightTheme["example2"])


        val textStyle by attr()
        val defaultColor by attr()

        // You can create paths referencing something with dotted string.
        // This is an attribute like 'textStyle' above,
        // but knows that there are some limitations to multi-path reference.
        val colorsColorPrimary = ref("colors.colorPrimary")
    }
}
