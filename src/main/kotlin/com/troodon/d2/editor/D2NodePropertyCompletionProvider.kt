package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

class D2NodePropertyCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val nodeBlockInfo = getNodeBlockInfo(parameters)
        if (nodeBlockInfo != null) {
            val definedProperties = getDefinedPropertiesInNode(nodeBlockInfo)
            NODE_PROPERTIES.forEach { property ->
                // Only suggest properties that aren't already defined
                if (!definedProperties.contains(property)) {
                    result.addElement(LookupElementBuilder.create(property))
                }
            }
        }
    }

    private data class NodeBlockInfo(val startOffset: Int, val content: String)

    private fun getNodeBlockInfo(parameters: CompletionParameters): NodeBlockInfo? {
        val text = parameters.originalFile.text
        val offset = parameters.offset

        // Look backwards from cursor to find if we're inside a node block
        val textBeforeCursor = text.substring(0, offset)

        // Find the last node definition before cursor: "nodeName: {"
        val nodePattern = Regex("""(\w+):\s*\{""")
        val matches = nodePattern.findAll(textBeforeCursor)

        if (matches.lastOrNull() != null) {
            val lastNodeMatch = matches.last()
            val afterLastNode = textBeforeCursor.substring(lastNodeMatch.range.last + 1)

            // Count braces to ensure we're still inside the node block
            var openBraces = 0
            afterLastNode.forEach { char ->
                when (char) {
                    '{' -> openBraces++
                    '}' -> openBraces--
                }
            }

            // We're inside if we haven't closed all braces
            if (openBraces >= 0) {
                // Extract the content of the node block so far
                return NodeBlockInfo(lastNodeMatch.range.last + 1, afterLastNode)
            }
        }

        return null
    }

    private fun getDefinedPropertiesInNode(nodeBlockInfo: NodeBlockInfo): Set<String> {
        val definedProperties = mutableSetOf<String>()

        // Match property definitions: "propertyName:"
        val propertyPattern = Regex("""^\s*(\w+):\s*""", RegexOption.MULTILINE)
        propertyPattern.findAll(nodeBlockInfo.content).forEach { match ->
            val propertyName = match.groupValues[1]
            if (NODE_PROPERTIES.contains(propertyName)) {
                definedProperties.add(propertyName)
            }
        }

        return definedProperties
    }

    companion object {
        private val NODE_PROPERTIES = listOf(
            "shape",
            "icon",
            "style",
            "label"
        )
    }
}
