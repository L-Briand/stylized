# Stylized Kotlin

A kotlin-multiplatform library to help you stylize your application and services.

## Motivations:

I used [Jetpack compose](https://developer.android.com/compose) a bit, but I thought it was missing a lot on
customization. When theming is involved, every little component takes bazillion of parameters to customize. Or you have
something looking like a theme manager which is too complex to use. It may be based, but it was my experience.

So my thought was, what if we only pass one parameter containing a "style" with everything inside instead of dozen of
parameters, somewhat similar to the Android XML theming system?

I find the principle of the theming system in Android to be quite powerful. Although limited, it is used in many cases.
Dimensions, Colors, View styling, Localization, and more. This library mimics the concept, adds flexibility on
declaration and usage. 

# Import from maven

You can find the library on maven central.

For Jvm:

```kotlin
dependencies {
    implementation("net.orandja.kt:stylized:0.0.1")
}
```

For multiplatform:

```kotlin
kotlin {
    ...
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("net.orandja.kt:stylized:0.0.1")
            }
        }
    }
}
```

# How to use

The saying "An example is worth a thousand words" goes well here. So there it is.

Color theming is the simplest way to understand the concept and principle. Let's assume a simple set of colors like so:

```kotlin
enum class Color { GREEN, BLUE, WHITE }
```

## Attributes

Almost everything starts by declaring attributes.

```kotlin
val white by attr() // Simple attribute. Reference itself. (Same as: attr { it["white"] })
val blue by attr(Color.BLUE) // Reference an object. It can be anything.
val colorPrimary by attr(blue) // Reference a reference.
val colorBackground by attr { it["green"] } // Reference an unknown attribute.

// You can 'get' the value of an attribute
val blueColor = blue.get<Color>()
assertEquals(Color.BLUE, blueColor)
assertEquals(blueColor, colorPrimary.get())
```

## Styles

Attributes can be used as building blocks for styles, as keys and as values.
Styles are attributes themselves and can be used like attributes too.
More on that later.

```kotlin
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
```

## Styles Customization

Here's the good part. Styles can have a parent and inherit their values. Making variation of a style simple.

```kotlin
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
```

## Style Composition

If before it was good, now it's fun. Styles can have substyles. Substyles know the attributes of their parents, if the
context allows it.

```kotlin
val textStyle by attr()
val defaultColor by attr()

// You can create paths referencing something with dotted string.
// This is an attribute like 'textStyle' above.
// You should know that there are some limitations to multi-path reference.
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
    // 'colors' is a style, but it is used as an attribute here.
    // Dereferencing is mandatory, otherwise colors would be a reference to `lightTheme`
    // and `lightTheme` is not in the style.
    // Colors now have lightTheme elements in it.
    colors set lightTheme.dereference()

    // Multi-path reference can be used as an attribute to setup complex path reference.
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
```

## Limitations

1. You cannot create `set` an attribute with a multipath-(reference/attribute).

```kotlin
val `foo.bar` by attr()
val fooBar = reference("foo.bar")

val baz by style {
    assertFails { `foo.bar` set 33 }
    assertFails { fooBar set 33 }
} 
```

2. A substyle does not know of its parent by itself.

```kotlin
val foo by style {
    "bar" set 33
    "baz" set group() {
        "qux" set reference("bar")
    }
}
// This works because 'foo' is used to get 'qux'
assertTrue(33, foo["baz.qux"])

// This doesn't work because 'baz' (once "alone") knows nothing about 'foo'.
assertFails { foo.get<Style>("baz").get<Int>("qux") }
```

To better understand, you can write it this way, its more obvious as to why it does not work.

```kotlin 
val baz by style {
    "qux" set reference("bar")
}

val foo by style {
    "bar" set 33
    +baz
}

assertFails { baz.get<Int>("qux") }
```

