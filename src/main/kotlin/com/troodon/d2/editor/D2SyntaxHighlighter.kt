package com.troodon.d2.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.tree.IElementType
import com.troodon.d2.lang.D2Types
import com.troodon.d2.lexer.D2Lexer

class D2SyntaxHighlighter : SyntaxHighlighter {

    override fun getHighlightingLexer(): Lexer = D2Lexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        when (tokenType) {
            D2Types.IDENTIFIER -> IDENTIFIER_KEYS
            D2Types.STRING -> STRING_KEYS
            D2Types.COMMENT, D2Types.BLOCK_COMMENT -> COMMENT_KEYS
            D2Types.ARROW, D2Types.ARROW_LEFT, D2Types.ARROW_BOTH, D2Types.CONNECTION -> OPERATOR_KEYS
            D2Types.COLON, D2Types.SEMICOLON -> COLON_KEYS
            D2Types.PIPE, D2Types.STAR -> OPERATOR_KEYS
            D2Types.LBRACE, D2Types.RBRACE -> BRACE_KEYS
            D2Types.LPAREN, D2Types.RPAREN -> PARENTHESIS_KEYS
            D2Types.LBRACKET, D2Types.RBRACKET -> BRACKET_KEYS
            D2Types.NUMBER -> NUMBER_KEYS
            D2Types.KEYWORD -> KEYWORD_KEYS
            else -> emptyArray()
        }

    companion object {
        val IDENTIFIER_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_IDENTIFIER",
                DefaultLanguageHighlighterColors.IDENTIFIER
            )
        )

        val STRING_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_STRING",
                DefaultLanguageHighlighterColors.STRING
            )
        )

        val COMMENT_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_COMMENT",
                DefaultLanguageHighlighterColors.LINE_COMMENT
            )
        )

        val OPERATOR_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_OPERATOR",
                DefaultLanguageHighlighterColors.OPERATION_SIGN
            )
        )

        val COLON_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_COLON",
                DefaultLanguageHighlighterColors.COMMA
            )
        )

        val NUMBER_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_NUMBER",
                DefaultLanguageHighlighterColors.NUMBER
            )
        )

        val KEYWORD_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_KEYWORD",
                DefaultLanguageHighlighterColors.KEYWORD
            )
        )

        val BRACE_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_BRACES",
                DefaultLanguageHighlighterColors.BRACES
            )
        )

        val PARENTHESIS_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_PARENTHESES",
                DefaultLanguageHighlighterColors.PARENTHESES
            )
        )

        val BRACKET_KEYS = arrayOf(
            TextAttributesKey.createTextAttributesKey(
                "D2_BRACKETS",
                DefaultLanguageHighlighterColors.BRACKETS
            )
        )
    }
}