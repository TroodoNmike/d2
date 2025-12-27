package com.troodon.d2.preview

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBSplitter
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class D2SplitEditor(
    private val textEditor: TextEditor,
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor {

    private val splitter: JBSplitter
    private val previewPanel: D2PreviewPanel
    private var currentLayout: D2EditorLayout = D2EditorLayout.EDITOR_AND_PREVIEW

    init {
        previewPanel = D2PreviewPanel(project, file, textEditor.editor)
        Disposer.register(this, previewPanel)

        splitter = JBSplitter(false, 0.5f).apply {
            firstComponent = textEditor.component
            secondComponent = previewPanel.component
        }

        // Listen to document changes
        textEditor.editor.document.addDocumentListener(previewPanel.documentListener, this)
    }

    fun setLayout(layout: D2EditorLayout) {
        currentLayout = layout
        when (layout) {
            D2EditorLayout.EDITOR_ONLY -> {
                splitter.firstComponent = textEditor.component
                splitter.secondComponent = null
            }
            D2EditorLayout.EDITOR_AND_PREVIEW -> {
                splitter.firstComponent = textEditor.component
                splitter.secondComponent = previewPanel.component
                splitter.proportion = 0.5f
            }
            D2EditorLayout.PREVIEW_ONLY -> {
                splitter.firstComponent = null
                splitter.secondComponent = previewPanel.component
            }
        }
    }

    fun getLayout(): D2EditorLayout = currentLayout

    override fun getComponent(): JComponent = splitter

    override fun getPreferredFocusedComponent(): JComponent? = textEditor.preferredFocusedComponent

    override fun getName(): String = "D2 Editor"

    override fun setState(state: FileEditorState) {
        textEditor.setState(state)
    }

    override fun isModified(): Boolean = textEditor.isModified

    override fun isValid(): Boolean = textEditor.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        textEditor.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        textEditor.removePropertyChangeListener(listener)
    }

    override fun getCurrentLocation(): FileEditorLocation? = textEditor.currentLocation

    override fun dispose() {
        Disposer.dispose(textEditor)
    }

    override fun getFile(): VirtualFile = file
}


