package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext

class D2IdentifierCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val currentNodeName = getCurrentNodeContext(parameters)
        val identifiers = collectIdentifiers(parameters.originalFile)

        identifiers.forEach { identifier ->
            // Exclude the current node name from autocomplete
            if (identifier != currentNodeName) {
                result.addElement(LookupElementBuilder.create(identifier))
            }
        }
    }

    private fun getCurrentNodeContext(parameters: CompletionParameters): String? {
        val text = parameters.originalFile.text
        val offset = parameters.offset

        // Look backwards from cursor to find if we're inside a node block
        val textBeforeCursor = text.substring(0, offset)

        // Find the last node definition before cursor: "nodeName: {"
        val nodePattern = Regex("""(\w+):\s*\{[^}]*$""")
        val match = nodePattern.findAll(textBeforeCursor).lastOrNull()

        return match?.groupValues?.get(1)
    }

    private fun collectIdentifiers(file: PsiElement): Set<String> {
        val identifiers = mutableSetOf<String>()
        val text = file.text
        // Match identifiers before ':' (object definitions)
        val objectRegex = Regex("""^(\w+):\s*\{""", RegexOption.MULTILINE)
        objectRegex.findAll(text).forEach { match ->
            identifiers.add(match.groupValues[1])
        }
        // Match identifiers in connections (e.g., "one -> two")
        val connectionRegex = Regex("""(\w+)\s*->\s*(\w+)""")
        connectionRegex.findAll(text).forEach { match ->
            identifiers.add(match.groupValues[1])
            identifiers.add(match.groupValues[2])
        }
        return identifiers
    }
}
