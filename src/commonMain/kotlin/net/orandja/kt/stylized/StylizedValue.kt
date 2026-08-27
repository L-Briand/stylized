package net.orandja.kt.stylized

import kotlin.jvm.JvmInline


/**
 * Hold a single value that can be fetched from a [Node].
 */
@JvmInline
value class StylizedValue<T>(private val value: T) : Stylized.Value<T> {
    override fun get(node: Node): T = value
    override fun toString(): String = value.toString()
}