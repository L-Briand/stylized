package net.orandja.kt.stylized

import net.orandja.kt.stylized.Stylized.Reference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


/**
 * Most of the DSL in stylized works with the kotlin's `by` keyword.
 * This interface tells that the reference is a property and should be initialized lazily.
 */
interface LazyReference : Reference, ReadOnlyProperty<Any?, Reference> {
    var isInitialized: Boolean
    fun initialize(name: List<String>)
}

/**
 * This interface represents a reference that has a type associated with it.
 * This is useful when creating a typed reference.
 */
interface TypedReference<T: Any, S : T?> : Reference {
    val type: KClass<T>
    val nullable: Boolean
}

interface LazyTypedReference<T : Any> : LazyReference, TypedReference<T, T?>

interface StylizedReference {

    // TODO: Documentation
    class UnknownReference(override val identity: List<String>) : Reference {
        override fun get(node: Node): Stylized? {
            if (node.hasVisited(this)) throw CircularReferenceException(this, node)
            node.setVisited(this)
            return node.getOrNull(identity)
        }

        override fun toString(): String = "*${identity.joinToString(".") { it }}?"
    }

    class UnknownTypedReference<T : Any>(override val identity: List<String>, override val type: KClass<T>) :
        TypedReference<T> {
        override fun get(node: Node): Stylized? {
            if (node.hasVisited(this)) throw CircularReferenceException(this, node)
            node.setVisited(this)
            return node.getOrNull(identity)
        }

        override fun toString(): String = "*${identity.joinToString(".") { it }}?"
    }

    // TODO: Documentation
    class ValueReference(override val identity: List<String>, val value: Stylized) : Reference {
        override fun get(node: Node): Stylized = value
        override fun toString(): String = "*${identity.joinToString(".") { it }}: $value"
    }

    // TODO: Documentation
    class CallbackReference(override val identity: List<String>, val callback: (Node) -> Stylized) :
        Reference {
        override fun get(node: Node): Stylized = callback(node)
        override fun toString(): String = "*${identity.joinToString(".") { it }} {?}"
    }

    // TODO: Documentation
    class FallbackReference(val fallback: Reference) : Reference {
        override fun get(node: Node): Stylized? {
            if (node.hasVisited(this)) return fallback.get(node)
            node.setVisited(this)
            return node.getOrNull(identity) ?: fallback.get(node)
        }

        override val identity: List<String> get() = fallback.identity
        override fun toString(): String = "^$fallback"
    }

    // TODO: Documentation
    class LazyUnknownReference<T : Any>(override val type: KClass<T>) : LazyReference, TypedReference<T> {
        override var isInitialized: Boolean = false
        private var name: List<String>? = null

        override fun initialize(name: List<String>) {
            if (isInitialized) return
            this.name = name
            isInitialized = true
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Reference {
            initialize(tokenizeDottedString(property.name))
            return this
        }

        override fun get(node: Node): Stylized? {
            if (!isInitialized) throw ReferenceNotInitializedException()
            if (node.hasVisited(this)) throw CircularReferenceException(this, node)
            node.setVisited(this)
            return node.getOrNull(name!!)
        }

        override val identity: List<String>
            get() {
                if (!isInitialized) throw ReferenceNotInitializedException()
                return name!!
            }

        override fun toString(): String = if (isInitialized) "!${name!!.joinToString(".") { it }}?" else "*?"
    }

    // TODO: Documentation
    abstract class LazyValueReference<T>(override val type: KClass<T>) : LazyReference, TypedReference<T> {

        abstract fun createStyle(name: List<String>): Stylized?

        override var isInitialized: Boolean = false
        var name: List<String>? = null
        var value: Stylized? = null

        override fun initialize(name: List<String>) {
            if (isInitialized) return
            this.name = name
            this.value = createStyle(name)
            isInitialized = true
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Reference {
            initialize(tokenizeDottedString(property.name))
            return this
        }

        override fun get(node: Node): Stylized? {
            if (!isInitialized) throw ReferenceNotInitializedException()
            return value!!
        }

        override val identity: List<String>
            get() {
                if (!isInitialized) throw ReferenceNotInitializedException()
                return name!!
            }

        override fun toString(): String = if (isInitialized) "!${name!!.joinToString(".") { it }}: ${value!!}" else "*?"
    }

    // TODO: Documentation
    abstract class LazyCallbackReference<T>(override val type: KClass<T>) : LazyReference, TypedReference<T> {

        abstract fun fetchStyle(name: List<String>, node: Node): Stylized?

        override var isInitialized: Boolean = false
        var name: List<String>? = null

        override fun initialize(name: List<String>) {
            if (isInitialized) return
            this.name = name
            isInitialized = true
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Reference {
            initialize(tokenizeDottedString(property.name))
            return this
        }

        override fun get(node: Node): Stylized? {
            if (!isInitialized) throw ReferenceNotInitializedException()
            return fetchStyle(name!!, node)
        }

        override val identity: List<String>
            get() {
                if (!isInitialized) throw ReferenceNotInitializedException()
                return name!!
            }

        override fun toString(): String = if (isInitialized) "!${name!!.joinToString(".") { it }}: {?}" else "*?"
    }

}


