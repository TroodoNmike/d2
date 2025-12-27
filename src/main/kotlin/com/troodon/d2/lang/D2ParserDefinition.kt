package com.troodon.d2.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.troodon.d2.lexer.D2Lexer

class D2ParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = D2Lexer()

    override fun createParser(project: Project?): PsiParser = D2Parser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = TokenSet.create(D2Types.COMMENT, D2Types.BLOCK_COMMENT)

    override fun getStringLiteralElements(): TokenSet = TokenSet.create(D2Types.STRING)

    override fun createElement(node: ASTNode?): PsiElement {
        throw UnsupportedOperationException("createElement not implemented")
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return D2PsiFile(viewProvider)
    }

    companion object {
        val FILE = IFileElementType(D2Language.INSTANCE)
    }
}
