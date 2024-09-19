package net.orandja.kt.stylized

import net.orandja.kt.stylized.Style.*
import net.orandja.kt.stylized.dsl.StyleBuilder
import net.orandja.kt.stylized.dsl.StyleGroupBuilder
import net.orandja.kt.stylized.exceptions.ReferenceNotFoundException

fun reference(vararg reference: Any) = StyleBuilder.reference(*reference)
fun style(from: Style? = null, block: StyleGroupBuilder.() -> Unit) = StyleBuilder.style(from, block)
fun attr() = StyleBuilder.attr()
fun <T> attr(value: T) = StyleBuilder.attr(value)
fun <T> attr(block: StyleBuilder.(Node) -> T) = StyleBuilder.attr(block)

// Value resolver

@Suppress("UNCHECKED_CAST")
fun <T> Style.get(): T = this.accept(Node(this), ValueResolver) as T

@Suppress("UNCHECKED_CAST")
fun <T> Style.getOrNull(): T? = this.accept(Node(this), ValueResolver) as? T

@Suppress("UNCHECKED_CAST")
operator fun <T> Style.get(vararg reference: Any): T {
    var node = Node(this)
    return try {
        node = node.resolve(*reference) ?: throw ReferenceNotFoundException(*reference, node = node)
        node.current.accept(node, ValueResolver) as T
    } catch (e: ClassCastException) {
        throw ReferenceNotFoundException(reference, node = node, cause = e)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Style.getOrNull(vararg reference: Any): T? {
    val node = Node(this).resolve(*reference)
    return node?.current?.accept(node, ValueResolver) as? T
}

private object ValueResolver : Visitor<Node, Any?> {
    override fun <V> value(data: Node, value: Value<V>): Any? = value.get(data)
    override fun group(data: Node, group: Group): Any = group
    override fun reference(data: Node, reference: Reference): Any? = reference.get(data).accept(data, this)
}

// Dereferencing

fun Style.dereference(): Style = accept(Node(this), DeReferencer)

private object DeReferencer : Visitor<Node, Style> {
    override fun <V> value(data: Node, value: Value<V>): Style = value
    override fun group(data: Node, group: Group): Style = group
    override fun reference(data: Node, reference: Reference): Style = reference.get(data).accept(data, this)
}
