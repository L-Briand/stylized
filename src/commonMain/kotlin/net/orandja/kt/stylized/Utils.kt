package net.orandja.kt.stylized

import net.orandja.kt.stylized.Stylized.Reference
import net.orandja.kt.stylized.Stylized.Style
import net.orandja.kt.stylized.Stylized.Value
import net.orandja.kt.stylized.Stylized.Visitor


/** Follow the reference and return the value it points to */
internal object ValueResolver : Visitor<Node, Any?> {
    override fun <V> value(data: Node, value: Value<V>): Any? = value.get(data)
    override fun style(data: Node, style: Style): Any = style
    override fun reference(data: Node, reference: Reference): Any? = reference.get(data)?.accept(data, this)
}

/** A [Stylized.Visitor] that search a value in a style given the provided key String. */
internal object StyleResolver : Visitor<Pair<Node, String>, Stylized?> {
    override fun <V> value(data: Pair<Node, String>, value: Value<V>): Stylized? = null
    override fun style(data: Pair<Node, String>, style: Style): Stylized? =
        style.get(data.first, data.second)

    override fun reference(data: Pair<Node, String>, reference: Reference): Stylized? =
        reference.get(data.first)?.accept(data, this)
}

/** A [Stylized.Visitor] that transform the visited [Stylized] to a node */
internal object StyleAsNode : Visitor<Node, Node?> {
    override fun <V> value(data: Node, value: Value<V>): Node = Node(value, data)
    override fun style(data: Node, style: Style): Node = Node(style, data)
    override fun reference(data: Node, reference: Reference): Node? =
        reference.get(data)?.accept(data, this)
}

/** Like [ValueResolver] but returns the [Stylized] instead */
internal object DeReferencer : Visitor<Node, Stylized?> {
    override fun <V> value(data: Node, value: Value<V>): Stylized = value
    override fun style(data: Node, style: Style): Stylized = style
    override fun reference(data: Node, reference: Reference): Stylized? = reference.get(data)?.accept(data, this)
}

/**
 * Transform the given dotted string[str] to a sequence of string.
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
internal fun tokenizeDottedString(str: CharSequence?, separator: Char = '.'): List<String> {
    str ?: return emptyList()

    if (str.isEmpty()) return emptyList()

    var index = 0

    // dismiss characters that do nothing.
    while (index < str.length && (str[index] == '.' || str[index].isWhitespace())) index += 1
    if (index == str.length) return emptyList()

    val result = ArrayList<String>(1)
    var wordStart: Int = -1
    var wordEnd = 0

    while (index < str.length) {
        if (str[index] == separator) {
            if (wordStart != -1) result += str.substring(wordStart, wordEnd)
            wordStart = -1
        } else if (!str[index].isWhitespace()) {
            if (wordStart == -1) wordStart = index
            wordEnd = index + 1
        }
        index++
    }
    if (wordStart != -1) result += str.substring(wordStart, wordEnd)

    return result
}