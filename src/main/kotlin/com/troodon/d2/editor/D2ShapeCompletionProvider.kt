package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

class D2ShapeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        if (isAfterShapeColon(parameters)) {
            SHAPE_VALUES.forEach { shape ->
                result.addElement(LookupElementBuilder.create(shape))
            }
        }
    }

    private fun isAfterShapeColon(parameters: CompletionParameters): Boolean {
        val text = parameters.originalFile.text
        val offset = parameters.offset
        if (offset == 0) return false
        val beforeCursor = text.substring(0, offset)
        val lastLine = beforeCursor.lines().lastOrNull() ?: return false
        return lastLine.contains(Regex("""shape:\s*[a-zA-Z0-9_-]*$"""))
    }

    companion object {
        private val SHAPE_VALUES = listOf(
            "rectangle",
            "square",
            "page",
            "parallelogram",
            "document",
            "cylinder",
            "queue",
            "package",
            "step",
            "callout",
            "stored_data",
            "person",
            "diamond",
            "oval",
            "circle",
            "hexagon",
            "cloud",
            "c4-person"
        )
    }
}
