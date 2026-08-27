package net.orandja.kt.stylized

import net.orandja.kt.stylized.Stylized.Reference
import net.orandja.kt.stylized.StylizedReference.CallbackReference
import net.orandja.kt.stylized.StylizedReference.LazyCallbackReference
import net.orandja.kt.stylized.StylizedReference.LazyUnknownReference
import net.orandja.kt.stylized.StylizedReference.LazyValueReference
import net.orandja.kt.stylized.StylizedReference.UnknownReference
import net.orandja.kt.stylized.StylizedReference.ValueReference

/**
 * Create a loopback reference. Its name is what it tries to get.
 * This is useful when you use a reference as keys or redirection for styles dsl.
 * This is the concept behind the [attr] function without parameters.
 *
 * @see [attr]
 */
fun ref(vararg id: String): Reference = UnknownReference(id.toList())

/**
 * Create a reference that has a static name and static style.
 */
fun ref(vararg id: String, style: Stylized): Reference = ValueReference(id.toList(), style)

/**
 * Create a reference which can resolve the stylized value at when called.
 * This can be useful if the style you need is dictated by flags or other dynamic conditions
 * which are not available when declaring the style.
 */
fun ref(vararg id: String, callback: (Node) -> Stylized): Reference = CallbackReference(id.toList(), callback)

/**
 * Declare an attribute to be used for styling purposes.
 * You can use it to declare elements in styles:
 *
 * // TODO: Example
 *
 * Or as keys to get styles:
 *
 * // TODO: Example
 */
// TODO: attr functions should be typed so when used to get a value the type is known
inline fun <reified T : Any> attr(): LazyUnknownReference<T> = LazyUnknownReference(T::class)

/**
 * Create an attribute that has a defined value / default value.
 *
 * // TODO: Example
 */
// TODO: Verify for nullable [Stylized]
inline fun <reified T> attr(
    value: T
): LazyValueReference<T> =
    object : LazyValueReference<T>(T::class) {
        override fun createStyle(name: List<String>): Stylized = when (value) {
            is Stylized -> value
            is Stylized.None -> throw IllegalUseOfNone()
            else -> StylizedValue(value)
        }
    }

/**
 * Create an attribute that does not know its value until it is used.
 *
 * // TODO: Example
 */
// TODO: Verify for nullable [Stylized]
inline fun <reified T> attr(
    crossinline block: (Node) -> T
): LazyCallbackReference<T> =
    object : LazyCallbackReference<T>(T::class) {
        override fun fetchStyle(name: List<String>, node: Node): Stylized = when (val value = block(node)) {
            is Stylized -> value
            is Stylized.None -> throw IllegalUseOfNone()
            else -> StylizedValue(value)
        }
    }

/**
 * Like [attr] but with a [StylizedStyle] as value.
 */
inline fun style(
    from: Stylized? = null,
    crossinline block: StylizedStyle.() -> Unit
): LazyValueReference<StylizedStyle> =
    object : LazyValueReference<StylizedStyle>(StylizedStyle::class) {
        override fun createStyle(name: List<String>): Stylized = StylizedStyle(from).apply(block)
    }


// region DSL Tools

/** Transform to [Stylized.Value] any [value] which is not a [Stylized]. */
internal fun <T> assertStylized(value: T): Stylized = when (value) {
    is Stylized -> value
    else -> StylizedValue(value)
}

// TODO: ADD static object STYLE_NOT_FOUND. to tell that the style was not found



