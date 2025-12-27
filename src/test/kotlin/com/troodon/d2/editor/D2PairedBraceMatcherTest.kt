package com.troodon.d2.editor

import com.troodon.d2.lang.D2Types
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class D2PairedBraceMatcherTest {

    @Test
    fun `test brace pairs configuration`() {
        val matcher = D2PairedBraceMatcher()
        val pairs = matcher.pairs

        assertEquals(1, pairs.size)
        assertEquals(D2Types.LBRACE, pairs[0].leftBraceType)
        assertEquals(D2Types.RBRACE, pairs[0].rightBraceType)
        assertTrue(pairs[0].isStructural)
    }

    @Test
    fun `test isPairedBracesAllowedBeforeType returns true`() {
        val matcher = D2PairedBraceMatcher()
        assertTrue(matcher.isPairedBracesAllowedBeforeType(D2Types.LBRACE, null))
        assertTrue(matcher.isPairedBracesAllowedBeforeType(D2Types.LBRACE, D2Types.IDENTIFIER))
    }
}
