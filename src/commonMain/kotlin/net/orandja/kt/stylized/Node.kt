package net.orandja.kt.stylized

/**
 * Used by [Stylized] elements to be able to get a resource in the style's tree they are from.
 *
 * It contains the history of crossed [Stylized] elements represented by the [parent]'s [Node].
 */
class Node internal constructor(
    val current: Stylized,
    val parent: Node? = null,
) {

    override fun toString(): String =
        if (parent == null) "Node($current)" else "Node($current, parent=$parent)"

    /**
     * Try to resolve a [Node] corresponding to the sequence represented by [keys].
     * Either in itself or in its parent.
     *
     * Examples are better than words, so:
     *
     * With a group like this `foo@{ a: bar@{ b: 1, c: 2 }, c: 3, d: 4 }`:
     *
     * - In @foo with `keys = [a, b]` it returns `1`
     * - In @foo and @bar with `keys = [a]` it returns `@bar`
     *
     * - In @foo with `keys = [b]` it will return `null`
     * - In @bar with `keys = [b]` it will return `1`
     *
     * - In @foo with `keys = [c]` it will return `3`
     * - In @bar with `keys = [c]` it will return `2`
     *
     * - In @foo and @bar with `keys = [d]` it will return `4`
     */
    fun resolve(keys: Iterator<String>, checkOnParent: Boolean = true): Node? {
        if (!keys.hasNext()) return this
        return resolveDown(keys, keys.next(), checkOnParent)
    }

    private fun resolveDown(
        keys: Iterator<String>,
        key: String,
        checkOnParent: Boolean
    ): Node? {
        // We search for a matching style with the key
        val style = current.accept(this to key, StyleResolver)
        // We found a style matching the key. we only go deeper from now on.
        if (style != null) return style.accept(this, StyleAsNode)?.resolve(keys, false)
        // We didn't find a matching key. Maybe the parent node has it.
        if (checkOnParent) return parent?.resolveDown(keys, key, true)
        return null
    }

    private val visited by lazy { mutableSetOf<Stylized.Reference>() }
    fun hasVisited(reference: Stylized.Reference): Boolean =
        reference in visited || (parent?.hasVisited(reference) ?: false)

    fun setVisited(reference: Stylized.Reference) {
        visited.add(reference)
    }

    // Functions to get the actual style of a resolved reference.

    fun getOrNull(reference: String): Stylized? = resolve(tokenizeDottedString(reference).iterator())?.current
    fun getOrNull(vararg reference: String): Stylized? = resolve(reference.iterator())?.current
    fun getOrNull(reference: List<String>): Stylized? = resolve(reference.iterator())?.current
    fun getOrNull(reference: Stylized.Reference): Stylized? = getOrNull(reference.identity)

    operator fun get(reference: Stylized.Reference): Stylized = get(reference.identity)

    operator fun get(reference: List<String>): Stylized =
        resolve(reference.iterator())?.current ?: throw ReferenceNotFoundException(reference, node = this)

    operator fun get(vararg reference: String): Stylized =
        resolve(reference.iterator())?.current ?: throw ReferenceNotFoundException(reference.toList(), node = this)

    operator fun get(reference: String): Stylized =
        resolve(tokenizeDottedString(reference).iterator())?.current
            ?: throw ReferenceNotFoundException(listOf(reference), node = this)

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