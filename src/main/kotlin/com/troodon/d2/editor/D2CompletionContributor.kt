package com.troodon.d2.editor

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiFile
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
}

class D2TypedHandler : TypedHandlerDelegate() {
    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file.language != D2Language.INSTANCE) {
            return Result.CONTINUE
        }

        // Auto-trigger completion when typing identifiers
        // This dynamically picks up newly added identifiers as you type
        if (charTyped.isLetterOrDigit() || charTyped == '_') {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        return Result.CONTINUE
    }
}
