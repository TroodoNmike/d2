package com.troodon.d2.editor

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.troodon.d2.lang.D2Language

class D2CompletionContributor : CompletionContributor() {
    init {
        // Node property completion (shape, icon, style, label)
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(D2Language.INSTANCE),
            D2NodePropertyCompletionProvider()
        )

        // Shape completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(D2Language.INSTANCE),
            D2ShapeCompletionProvider()
        )

        // Identifier completion
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(D2Language.INSTANCE),
            D2IdentifierCompletionProvider()
        )
    }

    @Deprecated("Deprecated in Java")
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean {
        // Auto-trigger completion when typing identifiers
        // This dynamically picks up newly added identifiers as you type
        return typeChar.isLetterOrDigit() || typeChar == '_'
    }
}
