package net.orandja.kt.stylized.exceptions

import net.orandja.kt.stylized.Style

class ReferenceNotFoundException internal constructor(
    vararg reference: Any?,
    node: Style.Node,
    cause: Throwable? = null,
) : IllegalStateException(
    "Could not resolve reference: ${reference.joinToString(", ", "[", "]") { it.toString() }}. In node: $node", cause
)