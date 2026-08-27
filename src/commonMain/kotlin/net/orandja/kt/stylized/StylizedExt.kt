package net.orandja.kt.stylized

import net.orandja.kt.stylized.Stylized.Reference
import net.orandja.kt.stylized.StylizedReference.UnknownReference
import net.orandja.kt.stylized.StylizedReference.UnknownTypedReference
import kotlin.reflect.KClass


@Suppress("UNCHECKED_CAST")
internal fun <T: Any> Stylized.getInternal(reference: Iterable<String>, type: KClass<T>): T {
    var node = Node(this)
    node = node.resolve(reference.iterator()) ?: throw ReferenceNotFoundException(reference.toList(), node = node)
    val result = node.current.accept(node, ValueResolver)
    try {
        return result as T
    } catch (e: ClassCastException) {
        throw ReferenceNotFoundException(reference.toList(), node = node, cause = e)
    }
}
// region

@Suppress("UNCHECKED_CAST")
fun <T> Stylized.get(): T = this.accept(Node(this), ValueResolver) as T

@Suppress("UNCHECKED_CAST")
fun <T> Stylized.getOrNull(): T? = this.accept(Node(this), ValueResolver) as? T


operator fun <T> Stylized.get(reference: String): T = getInternal(tokenizeDottedString(reference))
operator fun <T> Stylized.get(vararg reference: String): T = getInternal(reference.asIterable())
operator fun <T> Stylized.get(reference: Iterable<String>): T = getInternal(reference)
operator fun <T> Stylized.get(reference: List<String>): T = getInternal(reference)

operator fun <T> Stylized.get(vararg reference: Reference): T {
    if (reference.isEmpty()) get() as T
    if (reference.size == 1) return getInternal(reference.first().identity)
    return getInternal(reference.flatMap { it.identity })
}

@Suppress("UNCHECKED_CAST")
fun <T> Stylized.getOrNull(vararg reference: String): T? {
    val node = Node(this).resolve(reference.iterator())
    return node?.current?.accept(node, ValueResolver) as? T
}

// Dereferencing

fun Stylized.dereference(): Stylized? = accept(Node(this), DeReferencer)

operator fun Reference.plus(other: Reference) =
    if (other is TypedReference<*>) UnknownTypedReference((identity + other.identity).toList(), other.type)
    else UnknownReference((identity + other.identity).toList())
