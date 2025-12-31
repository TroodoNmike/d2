package com.troodon.d2.preview

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.troodon.d2.lang.D2FileType

class D2SplitEditorProvider : FileEditorProvider, DumbAware {
    
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileType is D2FileType
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val textEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor
        val previewPanel = D2PreviewPanel(project, textEditor.editor)
        
        return object : TextEditorWithPreview(
            textEditor,
            object : UserDataHolderBase(), FileEditor {
                override fun getComponent() = previewPanel.component
                override fun getPreferredFocusedComponent() = previewPanel.component
                override fun getName() = "Preview"
                override fun setState(state: com.intellij.openapi.fileEditor.FileEditorState) {}
                override fun isModified() = false
                override fun isValid() = true
                override fun addPropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
                override fun removePropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
                override fun dispose() = previewPanel.dispose()
                override fun getFile() = file
            },
            "D2Editor"
        ) {
            init {
                textEditor.editor.document.addDocumentListener(previewPanel.documentListener, this)
            }
        }
    }

    override fun getEditorTypeId(): String = "d2-split-editor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}
