package net.orandja.kt.stylized

import kotlin.test.Test
import kotlin.test.assertContentEquals


class TokenizeDottedStringTest {
    companion object {
        private val separators = arrayOf(".", "..", " ..", ".. ", ". .", " . .", ". . ")
        private val blanks = arrayOf("", " ", /* NBSP */ " ", /* NNBSP */ " ")
    }

    private fun assertTokenize(expected: List<String>, string: String) {
        assertContentEquals(expected, tokenizeDottedString(string).toList(), "Input '$string''")
    }

    private fun <T> List<T>.joinedStrings() = buildString { for (item in this@joinedStrings) append(item.toString()) }
    private fun <T> allPermutationsVararg(vararg elements: T): Set<List<T>> = allPermutations(elements.toList())
    private fun <T> allPermutations(list: List<T>): Set<List<T>> {
        if (list.isEmpty()) return setOf(emptyList())

        val result: MutableSet<List<T>> = mutableSetOf()
        for (i in list.indices) {
            allPermutations(list - list[i]).forEach { item ->
                result.add(item + list[i])
            }
        }
        return result
    }

    private fun assertAllPermutations(expected: List<String>, vararg strings: String) {
        allPermutationsVararg(*strings).onEach { assertTokenize(expected, it.joinedStrings()) }
    }


    @Test
    fun resultsToEmpty() {
        for (blank in blanks) {
            assertTokenize(listOf(), blank)
            for (separator in separators) {
                assertAllPermutations(listOf(), separator, blank)
                assertAllPermutations(listOf(), blank, separator, separator)
                assertAllPermutations(listOf(), blank, blank, separator)
            }
        }
    }

    @Test
    fun singleItemList() {
        val key = "key"
        val expected = listOf("key")
        assertTokenize(listOf(key), key)
        for (blank in blanks) {
            assertAllPermutations(expected, blank, key)
            for (separator in separators) {
                assertAllPermutations(expected, blank, separator, key)
                assertAllPermutations(expected, separator, blank, separator, key)
            }
        }
    }

    @Test
    fun doubleItemList() {
        val k = "key"
        val v = "value"
        val expected = listOf(k, v)
        assertTokenize(expected, "$k.$v")
        for (blank in blanks) {
            allPermutationsVararg(k, blank, blank).onEach { sk ->
                allPermutationsVararg(v, blank, blank).onEach { sv ->
                    assertTokenize(expected, "${sk.joinedStrings()}.${sv.joinedStrings()}")
                }
            }
        }
        for (separator in separators) {
            allPermutationsVararg(k, separator).onEach { sk ->
                allPermutationsVararg(v, separator).onEach { sv ->
                    assertTokenize(expected, "${sk.joinedStrings()}.${sv.joinedStrings()}")
                }
            }
        }
    }

    @Test
    fun multiItemList() {
        val a = "a"
        val b = "b"
        val c = "c"
        val expected = listOf(a, b, c)
        assertTokenize(expected, "$a.$b.$c")
        for (blank in blanks) {
            allPermutationsVararg(a, blank, blank).onEach { sa ->
                allPermutationsVararg(b, blank, blank).onEach { sb ->
                    allPermutationsVararg(c, blank, blank).onEach { sc ->
                        assertTokenize(expected, "${sa.joinedStrings()}.${sb.joinedStrings()}.${sc.joinedStrings()}")
                    }
                }
            }
        }

        for (separator in separators) {
            allPermutationsVararg(a, separator).onEach { sa ->
                allPermutationsVararg(b, separator).onEach { sb ->
                    allPermutationsVararg(c, separator).onEach { sc ->
                        assertTokenize(expected, "${sa.joinedStrings()}.${sb.joinedStrings()}.${sc.joinedStrings()}")
                    }
                }
            }
        }
    }

    @Test
    fun spacesInKeysDoNothing() {
        assertTokenize(listOf("a b", "c"), "a b. c")
        assertTokenize(listOf("a", "b c"), "a.b c")
    }
}