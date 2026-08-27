package net.orandja.kt.stylized

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class BasicTests {
    enum class Color { GREEN, BLUE }

    @Test
    fun `get empty attribute`() {
        val attr by attr()
        assertFailsWith<CircularReferenceException> { attr.get<Color>() }
    }

    @Test
    fun `get attribute with default value`() {
        val attr by attr(Color.BLUE)
        assertEquals(Color.BLUE, attr.get())
    }

    @Test
    fun `get attribute with callback`() {
        val attr by attr { Color.BLUE }
        assertEquals(Color.BLUE, attr.get())
    }

    @Test
    fun `get empty chained attributes`() {
        val color by attr()
        val colorPrimary by attr(color)
        assertFailsWith<CircularReferenceException> { colorPrimary.get<Color>() }
    }

    @Test
    fun `get chained attributes with default value`() {
        val color by attr(Color.BLUE)
        val colorPrimary by attr(color)
        assertEquals(Color.BLUE, colorPrimary.get())
    }

    @Test
    fun `get chained attributes with callback`() {
        val color by attr { Color.BLUE }
        val colorPrimary by attr(color)
        assertEquals(Color.BLUE, colorPrimary.get())
    }

    @Test
    fun `circular reference 1`() {
        val style by style {
            "attr1" by ref("attr2")
            "attr2" by ref("attr1")
        }
        assertFailsWith<CircularReferenceException> { style["attr1"] }
    }

    @Test
    fun `circular reference 2`() {
        val attr1 by attr()
        val attr2 by attr()
        val style by style {
            attr1 by attr2
            attr2 by attr1
        }
        assertFailsWith<CircularReferenceException> { style[attr1] }
    }

    @Test
    fun `circular reference 3`() {
        val attr1 by attr()
        val attr2 by attr()
        val style by style {
            attr1 by style {
                attr2 by attr1
            }
        }
        assertFailsWith<CircularReferenceException> { style[attr1 + attr2] }
    }

    @Test
    fun `circular reference 4`() {
        val style by style {
            "attr1" by style {
                "value" by ref("attr2.value")
            }
            "attr2" by style {
                "value" by ref("attr1.value")
            }
        }
        assertFailsWith<CircularReferenceException> { style["attr1.value"] }
    }

    @Test
    fun `get attributes by chained references`() {
        val attr1 by attr(Color.BLUE)
        val attr2 by attr(attr1)
        assertEquals(Color.BLUE, attr2.get())
    }

    @Test
    fun `get attribute in style`() {
        val blue by attr()
        val style by style {
            blue by Color.BLUE
        }
        assertEquals(Color.BLUE, style[blue])
    }

    @Test
    fun `referencing by attribute in style`() {
        val blue by attr()
        val style by style {
            blue by Color.BLUE
            "color" by blue
        }
        assertEquals(Color.BLUE, style["color"])
    }

    @Test
    fun `get in simple tree`() {
        val style by style {
            "colors" by style {
                "primary" by Color.BLUE
            }
        }
        assertEquals(Color.BLUE, style["colors.primary"])
    }

    @Test
    fun `get from parent in tree`() {
        val blue by attr()
        val style by style {
            blue by Color.BLUE
            "colors" by style {
                "primary" by blue
            }
        }
        assertEquals(Color.BLUE, style["colors.primary"])
    }

    @Test
    fun dereference() {
        val blue by attr(Color.BLUE)
        val colorPrimary by attr(blue)
        val blue2 = colorPrimary.dereference()

        assertIs<Stylized.Reference>(blue)
        assertIs<Stylized.Value<Color>>(blue2)
        assertEquals(Color.BLUE, blue.get())
        assertEquals(Color.BLUE, blue2.get())
    }

    @Test
    fun `get parent attribute`() {
        val parent by style {
            "blue" by Color.BLUE
        }

        val child by style(parent) {
            "primary" by ref("blue")
        }

        assertEquals(Color.BLUE, child["primary"])
    }

    @Test
    fun `get override attribute`() {
        val blue by attr(Color.BLUE)
        val colorPrimary by attr(blue)
        val parent by style {
            "green" by Color.GREEN
        }
        val child by style(parent) {
            colorPrimary by ref("green")
            "colorSecondary" by colorPrimary
        }
        assertEquals(Color.GREEN, child["colorSecondary"])
    }

    @Test
    fun `get callback attribute`() {
        val blue by attr(Color.BLUE)
        val style by style {
            +blue
            "colorPrimary" by attr { it["blue"] }
        }
        assertEquals(Color.BLUE, style["colorPrimary"])
    }

    @Test
    fun `add style in style`() {
        val colors by style {
            "blue" by Color.BLUE
        }
        val style2 by style {
            +colors
        }
        assertEquals(Color.BLUE, style2["colors.blue"])
    }

    @Test
    fun `get by style and attribute`() {
        val attr by attr()
        val inner by style {
            attr by Color.GREEN
        }
        val outer by style {
            +inner
        }
        assertEquals(Color.GREEN, outer[inner, attr])
    }
}