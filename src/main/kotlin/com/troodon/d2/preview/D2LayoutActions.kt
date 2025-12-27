package com.troodon.d2.preview

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class D2EditorOnlyAction(private val editor: D2SplitEditor) : ToggleAction(
    "Editor only",
    "Show editor only",
    AllIcons.General.LayoutEditorOnly
) {
    override fun isSelected(e: AnActionEvent): Boolean {
        return editor.getLayout() == D2EditorLayout.EDITOR_ONLY
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) {
            editor.setLayout(D2EditorLayout.EDITOR_ONLY)
        }
    }
}

class D2EditorAndPreviewAction(private val editor: D2SplitEditor) : ToggleAction(
    "Editor and Preview",
    "Show editor and preview side by side",
    AllIcons.General.LayoutEditorPreview
) {
    override fun isSelected(e: AnActionEvent): Boolean {
        return editor.getLayout() == D2EditorLayout.EDITOR_AND_PREVIEW
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) {
            editor.setLayout(D2EditorLayout.EDITOR_AND_PREVIEW)
        }
    }
}

class D2PreviewOnlyAction(private val editor: D2SplitEditor) : ToggleAction(
    "Preview only",
    "Show preview only",
    AllIcons.General.LayoutPreviewOnly
) {
    override fun isSelected(e: AnActionEvent): Boolean {
        return editor.getLayout() == D2EditorLayout.PREVIEW_ONLY
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) {
            editor.setLayout(D2EditorLayout.PREVIEW_ONLY)
        }
    }
}
