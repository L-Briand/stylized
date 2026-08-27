package net.orandja.kt.stylized

sealed interface Stylized {

    data object None : Stylized {
        override fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out = throw IllegalUseOfNone()
    }

    /**
     * Visitor method
     * @see Visitor
     */
    fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out

    // Interface of stylized elements

    fun interface Value<out T> : Stylized {
        fun get(node: Node): T
        override fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out =
            visitor.value(data, this)
    }

    interface Style : Stylized {
        fun get(node: Node, key: String): Stylized?
        override fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out =
            visitor.style(data, this)
    }

    interface Reference : Stylized {
        fun get(node: Node): Stylized?
        val identity: List<String>
        override fun <In, Out> accept(data: In, visitor: Visitor<In, Out>): Out =
            visitor.reference(data, this)
    }

    /**
     * Visitor pattern for [Stylized] interface.
     * The goal is to reduce the number of if checks with direct function calls to the detriment of the stack.
     * This effectively speeds up the process if the visitor's already instantiated.
     *
     * A quick example; Instead of doing:
     *
     * ```kotlin
     * when(style) {
     *   is Value -> ...
     *   is Style -> ...
     *   ...
     * }
     * ```
     *
     * You do:
     *
     * ```kotlin
     * style.accept(object : Visitor {
     *   override fun value(value: Value<T>) = ...
     *   override fun style(style: Style = ...
     *   ...
     * })
     * ```
     *
     * It is (often ?) quicker to do it this way when dealing with trees.
     */
    interface Visitor<in In, out Out> {
        fun <V> value(data: In, value: Value<V>): Out
        fun style(data: In, style: Style): Out
        fun reference(data: In, reference: Reference): Out
    }
}