package net.orandja.kt.stylized

import net.orandja.kt.stylized.Style.Reference
import net.orandja.kt.stylized.dsl.StyleBuilder
import net.orandja.kt.stylized.dsl.StyleGroupBuilder
import kotlin.jvm.JvmInline
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@JvmInline
private value class Box<T>(val value: T)

internal fun <T> lazyReadOnlyProperty(init: (property: KProperty<*>) -> T): ReadOnlyProperty<Any?, T> =
    LazyReadOnlyProperty(init)

/** [StyleGroupBuilder.set] needs to know if the value was created with [StyleBuilder.attr], [StyleBuilder.style] or [StyleBuilder.path] */
internal interface StyleReadOnlyProperty<T> : ReadOnlyProperty<Any?, T>

private class LazyReadOnlyProperty<T>(val init: (property: KProperty<*>) -> T) : StyleReadOnlyProperty<T> {
    private var box: Box<T>? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (box == null) box = Box(init(property))
        return box!!.value
    }
}

/**
 * Transform the given dotted string [str] to a sequence of string.
 * Example: `"key.value"` -> `["key", "value"]`
 *
 * - All values are trimmed of whitespaces. `" a. b .c "` -> `["a", "b", "c"]`
 * - An empty point `"."`, multiple points `".."` produces nothing. -> `[]`
 * - Leading point(s) `".key"`, `"..key"` or trailing point(s) `"key."`, `"key.."` are omitted -> `["key"]`
 * - Multipoint between elements  `"key..value"` are like single point -> `["key", "value"]`
 *
 * @param str The dotted string to parse
 * @return A sequence of trimmed strings separated by dots.
 */
internal fun tokenizeDottedString(str: CharSequence): Sequence<String> = sequence {
    if (str.isEmpty()) return@sequence

    var index = 0

    // dismiss characters that do nothing.
    while (index < str.length && (str[index] == '.' || str[index].isWhitespace())) index += 1
    if (index == str.length) return@sequence

    var wordStart: Int = -1
    var wordEnd = 0

    while (index < str.length) {
        if (str[index] == '.') {
            if (wordStart != -1) yield(str.substring(wordStart, wordEnd))
            wordStart = -1
        } else if (!str[index].isWhitespace()) {
            if (wordStart == -1) wordStart = index
            wordEnd = index + 1
        }
        index++
    }
    if (wordStart != -1) yield(str.substring(wordStart, wordEnd))
}

internal fun Sequence<String>.joinAsDottedString(): String = joinAsDottedString(iterator())
internal fun Sequence<String>.sanitize(): Sequence<String> = flatMap { tokenizeDottedString(it) }
internal fun Any?.extractStrings(): Sequence<String> = sequence { extractStrings(this@extractStrings) }

private suspend fun SequenceScope<String>.extractStrings(value: Any?) {
    when (value) {
        null -> return
        is Reference -> extractStrings(value.name())
        is Array<*> -> for (n in value) extractStrings(n)
        is Iterator<*> -> while(value.hasNext()) extractStrings(value.next())
        is Iterable<*> -> for (n in value) extractStrings(n)
        is Collection<*> -> for (n in value) extractStrings(n)
        is List<*> -> for (n in value) extractStrings(n)
        else -> yield(value.toString())
    }
}

private fun joinAsDottedString(iterator: Iterator<String>): String {
    if (!iterator.hasNext()) return ""
    val sb = StringBuilder()
    do {
        sb.append(iterator.next())
        sb.append('.')
    } while (iterator.hasNext())
    return sb.substring(0, sb.length - 1)
}
