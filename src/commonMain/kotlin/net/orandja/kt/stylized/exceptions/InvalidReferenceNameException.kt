package net.orandja.kt.stylized.exceptions

import net.orandja.kt.stylized.Style

class InvalidReferenceNameException internal constructor(message: String, reference: Style.Reference) :
    IllegalStateException("$message. Provided reference: $reference")