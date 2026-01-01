package com.troodon.d2.lexer

import org.junit.Test
import kotlin.test.assertEquals

class D2LexerTest {

    private fun tokenize(text: String): List<Pair<String, String>> {
        val lexer = D2Lexer()
        lexer.start(text, 0, text.length, 0)
        
        val tokens = mutableListOf<Pair<String, String>>()
        while (lexer.tokenType != null) {
            val tokenText = text.substring(lexer.tokenStart, lexer.tokenEnd)
            val tokenType = lexer.tokenType.toString()
            tokens.add(tokenText to tokenType)
            lexer.advance()
        }
        
        return tokens
    }

    @Test
    fun `test simple arrow connection`() {
        val tokens = tokenize("x -> y")
        
        assertEquals(5, tokens.size)
        assertEquals("x" to "IDENTIFIER", tokens[0])
        assertEquals(" " to "WHITE_SPACE", tokens[1])
        assertEquals("->" to "ARROW", tokens[2])
        assertEquals(" " to "WHITE_SPACE", tokens[3])
        assertEquals("y" to "IDENTIFIER", tokens[4])
    }

    @Test
    fun `test comment`() {
        val tokens = tokenize("# This is a comment")
        
        assertEquals(1, tokens.size)
        assertEquals("# This is a comment" to "COMMENT", tokens[0])
    }

    @Test
    fun `test keywords`() {
        val tokens = tokenize("label: My Label\nstyle.fill: red")
        
        // First line: keyword, colon, whitespace, identifier, whitespace, identifier
        assertEquals("label" to "KEYWORD", tokens[0])
        assertEquals(":" to "COLON", tokens[1])
        assertEquals(" " to "WHITE_SPACE", tokens[2])
        assertEquals("My" to "IDENTIFIER", tokens[3])
    }

    @Test
    fun `test numbers`() {
        val tokens = tokenize("width: 100\nopacity: 0.5")
        
        // Find the number tokens
        val numberTokens = tokens.filter { it.second == "NUMBER" }
        assertEquals(2, numberTokens.size)
        assertEquals("100", numberTokens[0].first)
        assertEquals("0.5", numberTokens[1].first)
    }

    @Test
    fun `test string literals`() {
        val tokens = tokenize("label: \"Hello World\"")
        
        val stringTokens = tokens.filter { it.second == "STRING" }
        assertEquals(1, stringTokens.size)
        assertEquals("\"Hello World\"", stringTokens[0].first)
    }

    @Test
    fun `test braces`() {
        val tokens = tokenize("x: { }")
        
        assertEquals("x" to "IDENTIFIER", tokens[0])
        assertEquals(":" to "COLON", tokens[1])
        assertEquals(" " to "WHITE_SPACE", tokens[2])
        assertEquals("{" to "LBRACE", tokens[3])
        assertEquals(" " to "WHITE_SPACE", tokens[4])
        assertEquals("}" to "RBRACE", tokens[5])
    }

    @Test
    fun `test complex diagram`() {
        val code = """
            direction: down
            
            server -> client: Request {
              style.animated: true
            }
        """.trimIndent()
        
        val tokens = tokenize(code)
        
        // Verify we have the expected token types
        val tokenTypes = tokens.map { it.second }.toSet()
        assert(tokenTypes.contains("KEYWORD"))
        assert(tokenTypes.contains("ARROW"))
        assert(tokenTypes.contains("COLON"))
        assert(tokenTypes.contains("IDENTIFIER"))
        assert(tokenTypes.contains("LBRACE"))
        assert(tokenTypes.contains("RBRACE"))
    }

    @Test
    fun `test negative numbers`() {
        val tokens = tokenize("left: -10")
        
        val numberTokens = tokens.filter { it.second == "NUMBER" }
        assertEquals(1, numberTokens.size)
        assertEquals("-10", numberTokens[0].first)
    }

    @Test
    fun `test dotted identifiers`() {
        val tokens = tokenize("style.fill")
        
        // Should be parsed as a single identifier with dot
        val identifierTokens = tokens.filter { it.second == "IDENTIFIER" || it.second == "KEYWORD" }
        assertEquals(1, identifierTokens.size)
        assert(identifierTokens[0].first.contains("."))
    }

    @Test
    fun `test underscore identifiers`() {
        val tokens = tokenize("_private")
        
        assertEquals(1, tokens.size)
        assertEquals("_private" to "IDENTIFIER", tokens[0])
    }

    @Test
    fun `test underscore at start`() {
        val tokens = tokenize("__hidden: value")
        
        assertEquals("__hidden" to "IDENTIFIER", tokens[0])
        assertEquals(":" to "COLON", tokens[1])
    }

    @Test
    fun `test bad characters`() {
        val tokens = tokenize("x @ y")
        
        assertEquals("x" to "IDENTIFIER", tokens[0])
        assertEquals(" " to "WHITE_SPACE", tokens[1])
        assertEquals("@" to "BAD_CHARACTER", tokens[2])
        assertEquals(" " to "WHITE_SPACE", tokens[3])
        assertEquals("y" to "IDENTIFIER", tokens[4])
    }

    @Test
    fun `test unclosed string`() {
        val tokens = tokenize("label: \"Hello")
        
        // String should consume until end of input
        val stringTokens = tokens.filter { it.second == "STRING" }
        assertEquals(1, stringTokens.size)
        assertEquals("\"Hello", stringTokens[0].first)
    }

    @Test
    fun `test hyphenated keywords`() {
        val tokens = tokenize("grid-columns: 3\nfont-size: 14")
        
        assertEquals("grid-columns" to "KEYWORD", tokens[0])
        assertEquals(":" to "COLON", tokens[1])
        
        // Find font-size keyword
        val keywords = tokens.filter { it.second == "KEYWORD" }
        assert(keywords.any { it.first == "font-size" })
    }

    @Test
    fun `test boolean keywords`() {
        val tokens = tokenize("animated: true\nvisible: false")
        
        val keywords = tokens.filter { it.second == "KEYWORD" }
        assert(keywords.any { it.first == "true" })
        assert(keywords.any { it.first == "false" })
        assert(keywords.any { it.first == "animated" })
    }

    @Test
    fun `test empty input`() {
        val tokens = tokenize("")
        
        assertEquals(0, tokens.size)
    }

    @Test
    fun `test minus not followed by digit`() {
        val tokens = tokenize("x - y")
        
        assertEquals("x" to "IDENTIFIER", tokens[0])
        assertEquals(" " to "WHITE_SPACE", tokens[1])
        assertEquals("-" to "BAD_CHARACTER", tokens[2])
        assertEquals(" " to "WHITE_SPACE", tokens[3])
        assertEquals("y" to "IDENTIFIER", tokens[4])
    }

    @Test
    fun `test multiple bad characters`() {
        val tokens = tokenize("$ @ % &")
        
        // Should have: $, space, @, space, %, space, &
        val badCharTokens = tokens.filter { it.second == "BAD_CHARACTER" }
        assertEquals(4, badCharTokens.size)
        assertEquals("$", badCharTokens[0].first)
        assertEquals("@", badCharTokens[1].first)
        assertEquals("%", badCharTokens[2].first)
        assertEquals("&", badCharTokens[3].first)
    }

    // New D2 Syntax Features Tests

    @Test
    fun `test block comments`() {
        val tokens = tokenize("\"\"\"This is a block comment\"\"\"")
        
        assertEquals(1, tokens.size)
        assertEquals("\"\"\"This is a block comment\"\"\"" to "BLOCK_COMMENT", tokens[0])
    }

    @Test
    fun `test multiline block comments`() {
        val code = "\"\"\"Multi\nLine\nComment\"\"\""
        
        val tokens = tokenize(code)
        val blockComments = tokens.filter { it.second == "BLOCK_COMMENT" }
        assertEquals(1, blockComments.size)
    }

    @Test
    fun `test arrow left`() {
        val tokens = tokenize("y <- x")
        
        assertEquals(5, tokens.size)
        assertEquals("y" to "IDENTIFIER", tokens[0])
        assertEquals(" " to "WHITE_SPACE", tokens[1])
        assertEquals("<-" to "ARROW_LEFT", tokens[2])
        assertEquals(" " to "WHITE_SPACE", tokens[3])
        assertEquals("x" to "IDENTIFIER", tokens[4])
    }

    @Test
    fun `test arrow both directions`() {
        val tokens = tokenize("x <-> y")
        
        val arrows = tokens.filter { it.second == "ARROW_BOTH" }
        assertEquals(1, arrows.size)
        assertEquals("<->", arrows[0].first)
    }

    @Test
    fun `test basic connection`() {
        val tokens = tokenize("x -- y")
        
        val connections = tokens.filter { it.second == "CONNECTION" }
        assertEquals(1, connections.size)
        assertEquals("--", connections[0].first)
    }

    @Test
    fun `test semicolon separator`() {
        val tokens = tokenize("shape1; shape2")
        
        val semicolons = tokens.filter { it.second == "SEMICOLON" }
        assertEquals(1, semicolons.size)
        assertEquals(";", semicolons[0].first)
    }

    @Test
    fun `test single wildcard`() {
        val tokens = tokenize("*.style: value")
        
        assertEquals("*" to "STAR", tokens[0])
        assertEquals("." to "BAD_CHARACTER", tokens[1])
        assertEquals("style" to "KEYWORD", tokens[2])
    }

    @Test
    fun `test double wildcard`() {
        val tokens = tokenize("**.style")
        
        assertEquals("**" to "STAR", tokens[0])
    }

    @Test
    fun `test pipe character`() {
        val tokens = tokenize("|xml")
        
        assertEquals("|xml" to "PIPE", tokens[0])
    }

    @Test
    fun `test parentheses`() {
        val tokens = tokenize("func(arg)")
        
        assertEquals("func" to "IDENTIFIER", tokens[0])
        assertEquals("(" to "LPAREN", tokens[1])
        assertEquals("arg" to "IDENTIFIER", tokens[2])
        assertEquals(")" to "RPAREN", tokens[3])
    }

    @Test
    fun `test square brackets`() {
        val tokens = tokenize("array[0]")
        
        assertEquals("array" to "IDENTIFIER", tokens[0])
        assertEquals("[" to "LBRACKET", tokens[1])
        assertEquals("0" to "NUMBER", tokens[2])
        assertEquals("]" to "RBRACKET", tokens[3])
    }

    @Test
    fun `test single quote strings`() {
        val tokens = tokenize("label: 'Hello World'")
        
        val strings = tokens.filter { it.second == "STRING" }
        assertEquals(1, strings.size)
        assertEquals("'Hello World'", strings[0].first)
    }

    @Test
    fun `test escaped characters in strings`() {
        val tokens = tokenize("label: \"Hello\\\"World\"")
        
        val strings = tokens.filter { it.second == "STRING" }
        assertEquals(1, strings.size)
        assertEquals("\"Hello\\\"World\"", strings[0].first)
    }

    @Test
    fun `test shape type keywords`() {
        val tokens = tokenize("shape: circle\nshape: cylinder")
        
        val keywords = tokens.filter { it.second == "KEYWORD" }
        assert(keywords.any { it.first == "shape" })
        assert(keywords.any { it.first == "circle" })
        assert(keywords.any { it.first == "cylinder" })
    }

    @Test
    fun `test style property keywords`() {
        val tokens = tokenize("fill: red\nstroke-width: 2\nfont-size: 14")
        
        val keywords = tokens.filter { it.second == "KEYWORD" }
        assert(keywords.any { it.first == "fill" })
        assert(keywords.any { it.first == "stroke-width" })
        assert(keywords.any { it.first == "font-size" })
    }

    @Test
    fun `test arrow type keywords`() {
        val tokens = tokenize("source-arrowhead: diamond\ntarget-arrowhead: cf-many")
        
        val keywords = tokens.filter { it.second == "KEYWORD" }
        assert(keywords.any { it.first == "diamond" })
        assert(keywords.any { it.first == "cf-many" })
    }

    @Test
    fun `test complex D2 diagram with new syntax`() {
        val blockCommentPart = "\"\"\"Block comment\"\"\""
        val code = blockCommentPart + """
            x <-> y: bidirectional
            a <- b: backward
            c -- d: connection

            shape1; shape2; shape3

            *.style: {
              fill: blue
            }
        """.trimIndent()

        val tokens = tokenize(code)
        val tokenTypes = tokens.map { it.second }.toSet()

        assert(tokenTypes.contains("BLOCK_COMMENT"))
        assert(tokenTypes.contains("ARROW_BOTH"))
        assert(tokenTypes.contains("ARROW_LEFT"))
        assert(tokenTypes.contains("CONNECTION"))
        assert(tokenTypes.contains("SEMICOLON"))
        assert(tokenTypes.contains("STAR"))
        assert(tokenTypes.contains("KEYWORD"))
    }

    @Test
    fun `test numbers with units as identifiers`() {
        val tokens = tokenize("expense: 283.56PLN")

        // Should not have any NUMBER tokens, 283.56PLN should be an IDENTIFIER
        val numberTokens = tokens.filter { it.second == "NUMBER" }
        assertEquals(0, numberTokens.size)

        val identifiers = tokens.filter { it.second == "IDENTIFIER" }
        assert(identifiers.any { it.first == "283.56PLN" })
    }

    @Test
    fun `test complex value with numbers and operators`() {
        val tokens = tokenize("expenseElse -> expense: 283.56PLN \\/ 12 = 23.63PLN")

        // Numbers followed by letters should be identifiers
        val identifiers = tokens.filter { it.second == "IDENTIFIER" }
        assert(identifiers.any { it.first == "283.56PLN" })
        assert(identifiers.any { it.first == "23.63PLN" })

        // Plain numbers like 12 should still be recognized as numbers
        val numberTokens = tokens.filter { it.second == "NUMBER" }
        assert(numberTokens.any { it.first == "12" })
    }

    @Test
    fun `test pure numbers remain as numbers`() {
        val tokens = tokenize("width: 100\nopacity: 0.75\ntop: -5")

        val numberTokens = tokens.filter { it.second == "NUMBER" }
        assertEquals(3, numberTokens.size)
        assert(numberTokens.any { it.first == "100" })
        assert(numberTokens.any { it.first == "0.75" })
        assert(numberTokens.any { it.first == "-5" })
    }
}
