package com.troodon.d2.preview

import com.intellij.openapi.project.Project
import java.io.File
import javax.swing.JComponent

interface PreviewRenderer {
    fun getComponent(): JComponent
    fun render(sourceFile: File, outputFile: File)
    fun zoomIn()
    fun zoomOut()
    fun getFileExtension(): String
    fun dispose()
}
