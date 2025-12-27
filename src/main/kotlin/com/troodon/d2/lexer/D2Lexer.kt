package com.troodon.d2.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.troodon.d2.lang.D2Types

class D2Lexer : LexerBase() {

    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null

    companion object {
        private val KEYWORDS = setOf(
            // Basic keywords
            "label", "description", "desc", "style", "shape", "direction",
            "grid-columns", "grid-gap", "grid-rows",
            
            // Style properties
            "font", "font-size", "font-color", "bold", "italic", "underline",
            "border-radius", "fill", "fill-pattern", "stroke", "stroke-width", "stroke-dash",
            "animated", "opacity", "shadow", "multiple", "3d", "double-border",
            "text-transform",
            
            // Layout
            "width", "height", "top", "left", "near", "icon", "tooltip",
            "link", "class", "vars", "scenarios",
            
            // Booleans
            "true", "false",
            
            // Shape types
            "rectangle", "square", "circle", "page", "document", "cylinder", "queue",
            "package", "step", "callout", "stored_data", "person", "diamond", "oval",
            "hexagon", "cloud", "parallelogram", "trapezoid",
            
            // Arrow types
            "triangle", "arrow", "diamond", "box", "cross",
            "cf-one", "cf-one-required", "cf-many", "cf-many-required",
            
            // Other
            "filled"
        )
    }

    override fun start(
        buffer: CharSequence,
        startOffset: Int,
        endOffset: Int,
        initialState: Int
    ) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.tokenEnd = startOffset
        advance()
    }

    override fun advance() {
        tokenStart = tokenEnd

        if (tokenStart >= endOffset) {
            tokenType = null
            return
        }

        val c = buffer[tokenStart]

        // Whitespace
        if (c.isWhitespace()) {
            tokenEnd = tokenStart + 1
            tokenType = TokenType.WHITE_SPACE
        }
        // Block Comment (""")
        else if (buffer.startsWith("\"\"\"", tokenStart)) {
            tokenEnd = tokenStart + 3
            // Find closing """
            while (tokenEnd + 2 < endOffset) {
                if (buffer.startsWith("\"\"\"", tokenEnd)) {
                    tokenEnd += 3
                    break
                }
                tokenEnd++
            }
            // If we didn't find closing """, consume to end
            if (tokenEnd + 2 >= endOffset && !buffer.startsWith("\"\"\"", tokenEnd)) {
                tokenEnd = endOffset
            }
            tokenType = D2Types.BLOCK_COMMENT
        }
        // Line Comment (#)
        else if (c == '#') {
            tokenEnd = tokenStart + 1
            while (tokenEnd < endOffset && buffer[tokenEnd] != '\n') {
                tokenEnd++
            }
            tokenType = D2Types.COMMENT
        }
        // Arrows and Connections
        else if (buffer.startsWith("<->", tokenStart)) {
            tokenEnd = tokenStart + 3
            tokenType = D2Types.ARROW_BOTH
        }
        else if (buffer.startsWith("->", tokenStart)) {
            tokenEnd = tokenStart + 2
            tokenType = D2Types.ARROW
        }
        else if (buffer.startsWith("<-", tokenStart)) {
            tokenEnd = tokenStart + 2
            tokenType = D2Types.ARROW_LEFT
        }
        else if (buffer.startsWith("--", tokenStart)) {
            tokenEnd = tokenStart + 2
            tokenType = D2Types.CONNECTION
        }
        // Multiline String (|...|)
        else if (c == '|') {
            tokenEnd = tokenStart + 1
            // Check if this starts a multiline string (has content or newline after)
            if (tokenEnd < endOffset && (buffer[tokenEnd] == '\n' || !buffer[tokenEnd].isWhitespace())) {
                // This is a multiline string marker, consume until closing |
                var depth = 1
                while (tokenEnd < endOffset && depth > 0) {
                    if (buffer[tokenEnd] == '|') {
                        depth--
                    }
                    tokenEnd++
                }
            }
            tokenType = D2Types.PIPE
        }
        // String with single quotes
        else if (c == '\'') {
            tokenEnd = tokenStart + 1
            while (tokenEnd < endOffset && buffer[tokenEnd] != '\'') {
                if (buffer[tokenEnd] == '\\' && tokenEnd + 1 < endOffset) {
                    tokenEnd += 2 // Skip escaped character
                } else {
                    tokenEnd++
                }
            }
            if (tokenEnd < endOffset) tokenEnd++
            tokenType = D2Types.STRING
        }
        // String with double quotes
        else if (c == '"') {
            tokenEnd = tokenStart + 1
            while (tokenEnd < endOffset && buffer[tokenEnd] != '"') {
                if (buffer[tokenEnd] == '\\' && tokenEnd + 1 < endOffset) {
                    tokenEnd += 2 // Skip escaped character
                } else {
                    tokenEnd++
                }
            }
            if (tokenEnd < endOffset) tokenEnd++
            tokenType = D2Types.STRING
        }
        // Colon
        else if (c == ':') {
            tokenEnd = tokenStart + 1
            tokenType = D2Types.COLON
        }
        // Semicolon
        else if (c == ';') {
            tokenEnd = tokenStart + 1
            tokenType = D2Types.SEMICOLON
        }
        // Star/Wildcard
        else if (c == '*') {
            tokenEnd = tokenStart + 1
            // Check for double star **
            if (tokenEnd < endOffset && buffer[tokenEnd] == '*') {
                tokenEnd++
            }
            tokenType = D2Types.STAR
        }
        // Braces
        else if (c == '{') {
            tokenEnd = tokenStart + 1
            tokenType = D2Types.LBRACE
        }
        else if (c == '}') {
            tokenEnd = tokenStart + 1
            tokenType = D2Types.RBRACE
        }
        // Parentheses
        else if (c == '(') {
            tokenEnd = tokenStart + 1
            tokenType = D2Types.LPAREN
        }
        else if (c == ')') {
            tokenEnd = tokenStart + 1
            tokenType = D2Types.RPAREN
        }
        // Brackets
        else if (c == '[') {
            tokenEnd = tokenStart + 1
            tokenType = D2Types.LBRACKET
        }
        else if (c == ']') {
            tokenEnd = tokenStart + 1
            tokenType = D2Types.RBRACKET
        }
        // Number
        else if (c.isDigit() || (c == '-' && tokenStart + 1 < endOffset && buffer[tokenStart + 1].isDigit())) {
            tokenEnd = tokenStart
            if (c == '-') tokenEnd++
            while (tokenEnd < endOffset && buffer[tokenEnd].isDigit()) {
                tokenEnd++
            }
            // Handle decimals
            if (tokenEnd < endOffset && buffer[tokenEnd] == '.') {
                tokenEnd++
                while (tokenEnd < endOffset && buffer[tokenEnd].isDigit()) {
                    tokenEnd++
                }
            }
            tokenType = D2Types.NUMBER
        }
        // Identifier or Keyword
        else if (c.isLetter() || c == '_') {
            tokenEnd = tokenStart + 1
            while (tokenEnd < endOffset &&
                (buffer[tokenEnd].isLetterOrDigit() || buffer[tokenEnd] == '-' || buffer[tokenEnd] == '_' || buffer[tokenEnd] == '.')
            ) {
                tokenEnd++
            }
            val text = buffer.substring(tokenStart, tokenEnd)
            tokenType = if (KEYWORDS.contains(text)) D2Types.KEYWORD else D2Types.IDENTIFIER
        }
        // Backslash (for escaping)
        else if (c == '\\') {
            tokenEnd = tokenStart + 1
            // Skip the next character if it exists
            if (tokenEnd < endOffset) {
                tokenEnd++
            }
            tokenType = TokenType.WHITE_SPACE // Treat escaped chars as whitespace for now
        }
        // Fallback
        else {
            tokenEnd = tokenStart + 1
            tokenType = TokenType.BAD_CHARACTER
        }
    }

    override fun getTokenType(): IElementType? = tokenType
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getState(): Int = 0
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = endOffset
}