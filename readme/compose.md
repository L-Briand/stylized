Compose snippet :

```kotlin
// Declaring attributes
val padding by attr()
val textColor by attr(Color.Black)
val backgroundColor by attr(Color.White)

// Creating styles and variations with attributes
val baseTheme by style {
    padding set 8.dp
    +backgroundColor
    +textColor
}

val lightTheme by style(baseTheme) {
    backgroundColor set Color(0xFFFBFBFB)
    textColor set Color(0xFF0A0A0A)
}

val darkTheme by style(baseTheme) {
    backgroundColor set Color(0xFF0A0A0A)
    textColor set Color(0xFFFBFBFB)
}

// Creating component with only a style.
@Composable
fun Greeting(modifier: Modifier = Modifier, style: Style) {
    Box(Modifier.background(style.get<Color>(backgroundColor))) {
        Text(
            text = "Hello world",
            color = style[textColor],
            modifier = modifier
                .padding(style.get<Dp>(padding)),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LightThemePreview() {
    Greeting(style = lightTheme)
}

@Preview(showBackground = true)
@Composable
fun DarkThemePreview() {
    Greeting(style = darkTheme)
}
```

Result :

![Compose result image](compose.png)