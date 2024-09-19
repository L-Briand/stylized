package net.orandja.kt.stylized.dsl

import net.orandja.kt.stylized.*
import net.orandja.kt.stylized.Style.*

/**
 * A utility class to simplify the creation of [Style].
 */
open class StyleBuilder internal constructor() {
    companion object : StyleBuilder()

    /** Transform to [Style.Value] any [value] which is not a style. */
    fun <T> stylized(value: T) = when (value) {
        is Style -> value
        else -> value(value)
    }

    // Delegates creation

    open fun style(from: Style? = null, block: StyleGroupBuilder.() -> Unit) =
        lazyReadOnlyProperty { referenceOf(it.name, style = group(from, block)) }

    open fun attr() = lazyReadOnlyProperty { reference(it.name) }
    open fun <T> attr(value: T) = lazyReadOnlyProperty { referenceOf(it.name, style = stylized(value)) }
    open fun <T> attr(block: StyleBuilder.(Node) -> T) = lazyReadOnlyProperty { property ->
        reference(property.name) { stylized(block(this, it)) }
    }

    fun <T> value(value: T): Style = StyleValue(value)
    fun group(from: Style? = null, block: StyleGroupBuilder.() -> Unit): Group =
        StyleGroupBuilder(from).apply(block).build()

    fun reference(vararg reference: Any?) = SelfReference(*reference)
    fun reference(vararg reference: Any?, callback: StyleBuilder.(Node) -> Style): Reference =
        CallableReference(reference.extractStrings().sanitize().joinAsDottedString(), callback)

    fun referenceOf(style: Style) = StyleReference(style)
    fun referenceOf(vararg reference: Any, style: Style) =
        NamedReference(reference.extractStrings().sanitize().joinAsDottedString(), style)

}