package net.orandja.kt.stylized.dsl

import net.orandja.kt.stylized.Style
import net.orandja.kt.stylized.Style.*
import net.orandja.kt.stylized.extractStrings
import net.orandja.kt.stylized.joinAsDottedString
import net.orandja.kt.stylized.sanitize
import kotlin.jvm.JvmInline

@JvmInline
value class StyleValue<T>(private val value: T) : Value<T> {
    override fun get(node: Node): T = value
    override fun toString(): String = "$value"
}

@JvmInline
value class StyleReference(private val reference: Style) : Reference {
    override fun get(node: Node): Style = reference
    override fun toString(): String = "Reference($reference)"
}

class SelfReference(private vararg val value: Any?) : Reference {
    override fun get(node: Node): Style = node.get(*value)
    override fun name(): String = value.extractStrings().sanitize().joinAsDottedString()
    override fun toString(): String = "SelfReference(${name()})"
}

data class NamedReference(
    private val name: String,
    private val reference: Style,
) : Reference {
    override fun get(node: Node): Style = reference
    override fun name(): String = name
    override fun toString(): String = "Reference($name: $reference)"
}

internal class CallableReference(
    private val name: String,
    private val callback: StyleBuilder.(Node) -> Style,
) : StyleBuilder(), Reference {
    override fun get(node: Node): Style = callback(node)
    override fun name(): String = name
    override fun toString(): String = "CallableReference($name)"
}

internal sealed class StyleGroup : Group {

    private object StyleResolver : Visitor<Pair<Node, String>, Style?> {
        override fun <V> value(data: Pair<Node, String>, value: Value<V>): Style? = null
        override fun group(data: Pair<Node, String>, group: Group): Style? = group.get(data.first, data.second)
        override fun reference(data: Pair<Node, String>, reference: Reference): Style? =
            reference.get(data.first).accept(data, this)
    }

    internal data class Empty(
        private val parent: Style? = null
    ) : StyleGroup() {
        override fun keys(): Set<String> = emptySet()
        override fun get(node: Node, key: String): Style? = parent?.accept(node to key, StyleResolver)
        override fun toString(): String = "Group(from: $parent)"
    }

    internal data class Single(
        private val key: String,
        private val value: Style,
        val parent: Style? = null,
    ) : StyleGroup() {
        override fun keys(): Set<String> = setOf(key)
        override fun get(node: Node, key: String): Style? =
            if (key == this.key) value else parent?.accept(node to key, StyleResolver)

        override fun toString(): String = "Group($key: $value, from: $parent)"
    }

    internal data class Multi(
        private val styles: Map<String, Style>,
        private val parent: Style? = null,
    ) : StyleGroup() {
        override fun keys(): Set<String> = styles.keys
        override fun get(node: Node, key: String): Style? = styles[key] ?: parent?.accept(node to key, StyleResolver)
        override fun toString(): String =
            "Group(${styles.entries.joinToString { "${it.key}: ${it.value}" }} from: $parent)"
    }
}