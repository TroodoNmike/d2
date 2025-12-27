package com.troodon.d2.editor

import com.troodon.d2.lang.D2Types
import com.troodon.d2.lexer.D2Lexer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class D2SyntaxHighlighterTest {

    @Test
    fun `test highlighting lexer is D2Lexer`() {
        val highlighter = D2SyntaxHighlighter()
        val lexer = highlighter.highlightingLexer

        assertNotNull(lexer)
        assertTrue(lexer is D2Lexer)
    }

    @Test
    fun `test identifier highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val keys = highlighter.getTokenHighlights(D2Types.IDENTIFIER)

        assertEquals(1, keys.size)
        assertEquals("D2_IDENTIFIER", keys[0].externalName)
    }

    @Test
    fun `test string highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val keys = highlighter.getTokenHighlights(D2Types.STRING)

        assertEquals(1, keys.size)
        assertEquals("D2_STRING", keys[0].externalName)
    }

    @Test
    fun `test comment highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val commentKeys = highlighter.getTokenHighlights(D2Types.COMMENT)
        val blockCommentKeys = highlighter.getTokenHighlights(D2Types.BLOCK_COMMENT)

        assertEquals(1, commentKeys.size)
        assertEquals(1, blockCommentKeys.size)
        assertEquals("D2_COMMENT", commentKeys[0].externalName)
        assertEquals("D2_COMMENT", blockCommentKeys[0].externalName)
    }

    @Test
    fun `test arrow highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val arrowKeys = highlighter.getTokenHighlights(D2Types.ARROW)
        val arrowLeftKeys = highlighter.getTokenHighlights(D2Types.ARROW_LEFT)
        val arrowBothKeys = highlighter.getTokenHighlights(D2Types.ARROW_BOTH)
        val connectionKeys = highlighter.getTokenHighlights(D2Types.CONNECTION)

        assertEquals(1, arrowKeys.size)
        assertEquals(1, arrowLeftKeys.size)
        assertEquals(1, arrowBothKeys.size)
        assertEquals(1, connectionKeys.size)
        assertEquals("D2_OPERATOR", arrowKeys[0].externalName)
        assertEquals("D2_OPERATOR", arrowLeftKeys[0].externalName)
        assertEquals("D2_OPERATOR", arrowBothKeys[0].externalName)
        assertEquals("D2_OPERATOR", connectionKeys[0].externalName)
    }

    @Test
    fun `test colon and semicolon highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val colonKeys = highlighter.getTokenHighlights(D2Types.COLON)
        val semicolonKeys = highlighter.getTokenHighlights(D2Types.SEMICOLON)

        assertEquals(1, colonKeys.size)
        assertEquals(1, semicolonKeys.size)
        assertEquals("D2_COLON", colonKeys[0].externalName)
        assertEquals("D2_COLON", semicolonKeys[0].externalName)
    }

    @Test
    fun `test number highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val keys = highlighter.getTokenHighlights(D2Types.NUMBER)

        assertEquals(1, keys.size)
        assertEquals("D2_NUMBER", keys[0].externalName)
    }

    @Test
    fun `test keyword highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val keys = highlighter.getTokenHighlights(D2Types.KEYWORD)

        assertEquals(1, keys.size)
        assertEquals("D2_KEYWORD", keys[0].externalName)
    }

    @Test
    fun `test brace highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val lbraceKeys = highlighter.getTokenHighlights(D2Types.LBRACE)
        val rbraceKeys = highlighter.getTokenHighlights(D2Types.RBRACE)

        assertEquals(1, lbraceKeys.size)
        assertEquals(1, rbraceKeys.size)
        assertEquals("D2_BRACES", lbraceKeys[0].externalName)
        assertEquals("D2_BRACES", rbraceKeys[0].externalName)
    }

    @Test
    fun `test parenthesis highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val lparenKeys = highlighter.getTokenHighlights(D2Types.LPAREN)
        val rparenKeys = highlighter.getTokenHighlights(D2Types.RPAREN)

        assertEquals(1, lparenKeys.size)
        assertEquals(1, rparenKeys.size)
        assertEquals("D2_PARENTHESES", lparenKeys[0].externalName)
        assertEquals("D2_PARENTHESES", rparenKeys[0].externalName)
    }

    @Test
    fun `test bracket highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val lbracketKeys = highlighter.getTokenHighlights(D2Types.LBRACKET)
        val rbracketKeys = highlighter.getTokenHighlights(D2Types.RBRACKET)

        assertEquals(1, lbracketKeys.size)
        assertEquals(1, rbracketKeys.size)
        assertEquals("D2_BRACKETS", lbracketKeys[0].externalName)
        assertEquals("D2_BRACKETS", rbracketKeys[0].externalName)
    }

    @Test
    fun `test pipe and star highlighting`() {
        val highlighter = D2SyntaxHighlighter()
        val pipeKeys = highlighter.getTokenHighlights(D2Types.PIPE)
        val starKeys = highlighter.getTokenHighlights(D2Types.STAR)

        assertEquals(1, pipeKeys.size)
        assertEquals(1, starKeys.size)
        assertEquals("D2_OPERATOR", pipeKeys[0].externalName)
        assertEquals("D2_OPERATOR", starKeys[0].externalName)
    }

}
