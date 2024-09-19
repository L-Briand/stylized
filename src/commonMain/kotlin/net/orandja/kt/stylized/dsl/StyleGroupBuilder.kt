package net.orandja.kt.stylized.dsl

import net.orandja.kt.stylized.Style
import net.orandja.kt.stylized.Style.Reference
import net.orandja.kt.stylized.StyleReadOnlyProperty
import net.orandja.kt.stylized.exceptions.InvalidReferenceNameException
import net.orandja.kt.stylized.extractStrings
import net.orandja.kt.stylized.sanitize

open class StyleGroupBuilder(private val parent: Style?) : StyleBuilder() {

    protected val values = mutableMapOf<String, Style>()

    fun add(name: String, style: Style) {
        values[name] = style
    }

    fun add(reference: Reference) {
        add(reference.assertName(), reference)
    }

    operator fun Reference.unaryPlus() {
        add(assertName(), this)
    }

    infix fun <T> Reference.set(value: T) {
        if (value is StyleReadOnlyProperty<*>)
            error("'style' and 'attr' function should not be used in style creation. Please use 'group' instead")
        add(assertName(), useRefNameInsteadOfRef(value))
    }

    infix fun <T> String.set(value: T) {
        if (value is StyleReadOnlyProperty<*>)
            error("'style' and 'attr' function should not be used in style creation. Please use 'group' instead")
        add(this, useRefNameInsteadOfRef(value))
    }

    internal fun build(): Style.Group = when (values.size) {
        0 -> StyleGroup.Empty(parent)
        1 -> {
            val (key, value) = values.entries.first()
            StyleGroup.Single(key, value, parent)
        }

        else -> {
            StyleGroup.Multi(values, parent)
        }
    }

    // Tools

    private fun Reference.assertName(): String {
        val name = name().extractStrings().sanitize().toList()
        if(name.isEmpty()) throw InvalidReferenceNameException("Reference does not have a name", this)
        if(name.size >= 2) throw InvalidReferenceNameException(
            "To set a value, reference/attribute should be single ('foo' not 'foo.bar')", this
        )
        return name[0]
    }

    private fun <T> useRefNameInsteadOfRef(value: T) = when (value) {
        is Reference -> reference(value)
        else -> stylized(value)
    }
}