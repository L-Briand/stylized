package net.orandja.kt.stylized

import net.orandja.kt.stylized.exceptions.ReferenceNotFoundException

/**
 *
 */
sealed interface Style {

    // Interface of stylized elements

    fun interface Value<out T : Any?> : Style {
        fun get(node: Node): T
        override fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out = visitor.value(data, this)
    }

    fun interface Group : Style {
        fun get(node: Node, key: String): Style?
        fun keys(): Set<String> = emptySet()
        override fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out = visitor.group(data, this)
    }

    fun interface Reference : Style {
        fun get(node: Node): Style
        fun name(): String = ""
        override fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out = visitor.reference(data, this)
    }

    /**
     * Visitor pattern for [Style] interface.
     * The goal is to reduce the number of if checks with direct function calls to the detriments of memory.
     * This effectively speeds up the process if the visitor's already instantiated.
     *
     * A quick example; Instead of doing:
     *
     * ```kotlin
     * when(style) {
     *   is Value -> // ...
     *   // ...
     * }
     * ```
     *
     * You do:
     *
     * ```kotlin
     * style.accept(object : Visitor {
     *   override fun value(value: Value<T>) = // ...
     *   // ...
     * })
     * ```
     */
    interface Visitor<in In, out Out> {
        fun <V> value(data: In, value: Value<V>): Out
        fun group(data: In, group: Group): Out
        fun reference(data: In, reference: Reference): Out
    }

    /** Visitor method  */
    fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out


    /**
     * Used by stylized elements to be able to get a resource in the style tree they are from.
     *
     * In a case where you have something like `textSize: Reference(smallSize)`.
     * `smallSize` should be deducted from one of:
     *
     * - The parent in which `textSize` is included
     * - A super style of `textSize`
     *
     * So when [Reference.get] is triggered, it can use the provided [Node] to [resolve] `smallSize`.
     */
    class Node internal constructor(
        val current: Style, val parent: Node? = null
    ) {
        override fun toString(): String = if (parent == null) "Node($current)" else "Node($current, parent=$parent)"

        /**
         * Try to resolve a [Node] corresponding to the sequence represented by [reference].
         * Either in itself or in its parent.
         *
         * Examples are better than words, so:
         *
         * With a group like this `foo@{ a: bar@{ b: 1, c: 2 }, c: 3, d: 4 }`:
         *
         * - In @foo and @bar with `keys = [a, b]` it returns 1
         * - In @foo and @bar with `keys = [a]` it returns @bar
         *
         * - In @foo with `keys = [b]` it will return null
         * - In @bar with `keys = [b]` it will return 1
         *
         * - In @foo with `keys = [c]` it will return 3
         * - In @bar with `keys = [c]` it will return 2
         *
         * - In @foo and @bar with `keys = [d]` it will return 4
         */
        fun resolve(vararg reference: Any?): Node? = resolve(reference.extractStrings().sanitize().iterator(), true)

        private fun resolve(keys: Iterator<String>, checkOnParent: Boolean): Node? {
            if (!keys.hasNext()) return this
            return resolveDown(keys, keys.next(), checkOnParent)
        }

        private fun resolveDown(keys: Iterator<String>, key: String, checkOnParent: Boolean): Node? {
            // We search for a matching style with the key
            val style = current.accept(key, StyleResolver())
            // We found a style matching key. we only go deeper from now on.
            if (style != null) return style.accept(this, StyleAsNode()).resolve(keys, false)
            // We didn't find a matching key. Maybe the parent node has it.
            if (checkOnParent) return parent?.resolveDown(keys, key, true)
            return null
        }

        /** A [Visitor] that return the style of the specified key */
        private inner class StyleResolver : Visitor<String, Style?> {
            override fun <V> value(data: String, value: Value<V>): Style? = null
            override fun group(data: String, group: Group): Style? = group.get(this@Node, data)
            override fun reference(data: String, reference: Reference): Style? =
                reference.get(this@Node).accept(data, this)
        }

        /** A [Visitor] that transform the visited style to a node */
        inner class StyleAsNode : Visitor<Node, Node> {
            override fun <V> value(data: Node, value: Value<V>): Node = Node(value, data)
            override fun group(data: Node, group: Group): Node = Node(group, data)
            override fun reference(data: Node, reference: Reference): Node = reference.get(this@Node).accept(data, this)
        }

        // Functions to get the actual style of a resolved reference.

        operator fun get(vararg reference: Any?): Style {
            val node = resolve(*reference)
                ?: throw ReferenceNotFoundException(*reference, node = this)
            return node.current
        }

        fun getOrNull(vararg reference: Any): Style? = resolve(*reference)?.current

        // --

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Node

            if (current != other.current) return false
            if (parent != other.parent) return false

            return true
        }

        override fun hashCode(): Int {
            var result = current.hashCode()
            result = 31 * result + (parent?.hashCode() ?: 0)
            return result
        }
    }
}