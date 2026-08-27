package net.orandja.kt.stylized

import net.orandja.kt.stylized.Stylized.Reference
import net.orandja.kt.stylized.Stylized.Style
import net.orandja.kt.stylized.StylizedReference.FallbackReference

open class StylizedStyle(
    var parent: Stylized? = null,
    val styles: MutableMap<String, Stylized> = mutableMapOf(),
) : Style {

    override fun get(node: Node, key: String): Stylized? =
        styles[key] ?: run {
            val parent = parent ?: return null
            parent.accept(Node(parent, node) to key, StyleResolver)
        }

    fun add(name: String, style: Stylized) = addInternal(tokenizeDottedString(name).iterator(), style)
    fun add(reference: Reference) {
        val stylized = reference.dereference() ?: throw UnableToAddReferenceException(reference)
        addInternal(reference.identity.iterator(), stylized)
    }
    operator fun Reference.unaryPlus() = add(this)

    infix fun String.by(style: Stylized) = add(this, style)
    infix fun <T> String.by(value: T) = add(this, assertStylized(value))

    infix fun Reference.by(style: Stylized) = addInternal(this.identity.iterator(), style)
    infix fun <T> Reference.by(value: T) = addInternal(this.identity.iterator(), assertStylized(value))

    override fun toString(): String = buildString {
        append("{ ")
        for ((key, value) in styles) {
            append(key)
            append(": ")
            append(value)
            append(", ")
        }
        if(styles.isNotEmpty()) deleteRange(length - 2, length)
        append(" }")
        parent?.let {
            append(" + ")
            append(it)
        }
    }

    /**
     * Adds a stylized element to the hierarchical style's treerepresented by the provided iterator.
     * If intermediate levels in the path do not exist, they are created automatically.
     *
     * @param ref An iterator over the sequence of string tokens representing the dotted path to
     *            add the stylized element.
     * @param style The stylized element to add at the specified path.
     */
    protected fun addInternal(ref: Iterator<String>, style: Stylized) {
        if (!ref.hasNext()) return
        val name = ref.next()
        if (name.contains('.')) throw IllegalArgumentException("Reference name should not contains any dot `.`")
        if (!ref.hasNext()) {
            if(style is LazyReference) style.initialize(emptyList())
            if(style is Reference && style.identity.isNotEmpty()) {
                styles[name] = FallbackReference(style)
                return
            }
            styles[name] = style
            return
        }
        val child = StylizedStyle(this, mutableMapOf())
        child.addInternal(ref, style)
        styles[name] = child
    }
}
