package net.orandja.kt.stylized

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

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
            white set Color.WHITE // Attributes can be used as keys.
            "green" set Color.GREEN // You can declare a value in a style without attribute.
            "example" set blue // Attributes can be used as values.
        }

        // You can then 'get' the value of a style with a path.
        val _colorWhite = colors.get<Color>("white")
        val colorWhite: Color = colors[white] // Or use the get, "[]" operator if the type is known

        assertEquals(Color.WHITE, colorWhite)
        assertEquals(Color.BLUE, colors[blue])
        assertEquals(Color.GREEN, colors["green"])
        assertEquals(Color.BLUE, colors["example"])

        val green by attr()
        assertEquals(Color.GREEN, colors[green])


        val lightTheme by style(from = colors) {
            // Even if 'colorPrimary' has a value, we can redefine it.
            colorPrimary set reference("green")

            // 'white' does not have a default value, but it references to itself.
            // 'colors' contains 'white', so it will find it.
            colorBackground set white

            // just so you know:
            "example1" set colorPrimary // reference colorPrimary, which is green
            "example2" set colorPrimary.dereference() // reference the value of the attribute of colorPrimary, which is blue
        }

        assertEquals(blueColor, colorPrimary.get()) // 'colorPrimary' still is blue
        assertEquals(Color.GREEN, lightTheme[green]) // but 'colorPrimary' in lightTheme is green
        assertEquals(Color.WHITE, lightTheme[colorBackground])
        assertEquals(Color.GREEN, lightTheme["example1"])
        assertEquals(Color.BLUE, lightTheme["example2"])


        val textStyle by attr()
        val defaultColor by attr()

        // You can create paths referencing something with dotted string.
        // This is an attribute like 'textStyle' above,
        // but knows that there are some limitations to multi-path reference.
        val colorsColorPrimary = reference("colors.colorPrimary")

        // You can create paths to some value with anything that produces dotted strings.
        class Path {
            override fun toString(): String = "colors.colorPrimary"
        }

        val _example1 = reference(Path())

        // You can create paths by giving a list of attributes
        val _example2 = reference(colors, colorPrimary)

        // Or mixing sources of dotted strings.
        val _example3 = reference(colors, "colorPrimary")
        val _example4 = reference(colors, "color.primary", Path()) // colors.color.primary.colors.colorPrimary

        val buttonStyle by style {
            // Dereferencing is mandatory, otherwise colors would be a reference to `lightTheme`
            // and `lightTheme` is not in the style.
            // Colors now have lightTheme elements in it.
            colors set lightTheme.dereference()

            // Here even if defaultColor is
            defaultColor set colorsColorPrimary

            textStyle set group(/* from = otherGroup */) {
                // 'defaultColor' does not exist in textStyle context, but it exists in the parent.
                // So it will find it if we try to get it from 'buttonStyle'
                "h1TextColor" set defaultColor
                // Same here with "colors.blue"
                // 'buttonStyle' contains 'colors', which contains 'blue'.
                "h2TextColor" set reference("colors.blue")
            }
        }

        assertEquals(Color.GREEN, buttonStyle[colors, colorPrimary])
        assertEquals(Color.BLUE, buttonStyle[colors, blue])
        assertEquals(Color.GREEN, buttonStyle[textStyle, "h1TextColor"])
        assertEquals(Color.BLUE, buttonStyle[textStyle, "h2TextColor"])
    }
}
