package net.orandja.kt.stylized

/**
 * Exception indicating that a reference could not be resolved.
 *
 * @property reference The sequence of keys or identifiers that could not be resolved.
 * @property node The context in which resolution of the reference was attempted.
 *
 * @param cause An optional cause for the exception, explaining the underlying reason why the
 * reference could not be resolved.
 */
class ReferenceNotFoundException internal constructor(
    val reference: List<String>,
    val node: Node,
    cause: Throwable? = null,
) : IllegalStateException(
    "Could not resolve reference: ${reference.joinToString("', '", "['", "']") { it }}. In node: $node", cause
)

/**
 * Exception thrown when an operation is attempted on a reference that has not been initialized.
 *
 * @see LazyUnknownReference
 * @see LazyValueReference
 * @see LazyCallbackReference
 */
class ReferenceNotInitializedException :
    IllegalStateException("Unable to execute operation. Reference not initialized.")

/**
 * I'm bad at explaining, so I will give you an example.
 *
 * If you try to do this:
 * ```kotlin
 * val color by attr()
 * val myStyle by style {
 *     +color //or add(color)
 * }
 * ```
 * `myStyle[color]` will not resolve because `+color` means nothing.
 *
 * It's practically the same as doing :
 * ```kotlin
 * val myStyle by style {
 *     "color" by reference("color")
 * }
 * ```
 */
class UnableToAddReferenceException(reference: Stylized.Reference) :
    IllegalStateException("Unable to add reference $reference because it leads to nothing. See the documentation of UnableToAddReferenceException for more information.")

/**
 * Exception thrown when a circular reference is detected while resolving a [Stylized].
 *
 * @param reference The reference that caused the circular dependency.
 * @param node The [Node] in which the circular reference was detected.
 */
class CircularReferenceException(reference: Stylized.Reference, node: Node) :
    IllegalStateException("Circular reference detected by searching reference: $reference in node: $node")

/**
 * Exception thrown to indicate the illegal use of the [Stylized.None] value in a context where it is not allowed.
 *
 * [Stylized.None] is a special value used to represent the absence of a stylized element.
 * Attempting to leverage [Stylized.None] in an unsupported or unintended manner will result in this exception.
 *
 * This exception typically indicates a misuse in the logic or design, where a meaningful stylized
 * element is expected but [Stylized.None] is encountered instead.
 */
class IllegalUseOfNone :
    IllegalStateException("Illegal use of `Stylized.None`. None is a special value that represents the absence of a stylized element.")