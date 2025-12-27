package com.troodon.d2.lang

import com.troodon.d2.lexer.D2TokenType

object D2Types {
    val IDENTIFIER = D2TokenType("IDENTIFIER")
    val STRING = D2TokenType("STRING")
    val COMMENT = D2TokenType("COMMENT")
    val BLOCK_COMMENT = D2TokenType("BLOCK_COMMENT")
    val ARROW = D2TokenType("ARROW")
    val ARROW_LEFT = D2TokenType("ARROW_LEFT")
    val ARROW_BOTH = D2TokenType("ARROW_BOTH")
    val CONNECTION = D2TokenType("CONNECTION")
    val LBRACE = D2TokenType("LBRACE")
    val RBRACE = D2TokenType("RBRACE")
    val LPAREN = D2TokenType("LPAREN")
    val RPAREN = D2TokenType("RPAREN")
    val LBRACKET = D2TokenType("LBRACKET")
    val RBRACKET = D2TokenType("RBRACKET")
    val COLON = D2TokenType("COLON")
    val SEMICOLON = D2TokenType("SEMICOLON")
    val PIPE = D2TokenType("PIPE")
    val STAR = D2TokenType("STAR")
    val NUMBER = D2TokenType("NUMBER")
    val KEYWORD = D2TokenType("KEYWORD")
}
