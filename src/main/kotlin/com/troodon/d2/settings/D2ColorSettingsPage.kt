package com.troodon.d2.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.troodon.d2.editor.D2SyntaxHighlighter
import com.troodon.d2.lang.D2FileType
import javax.swing.Icon

class D2ColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon? = null

    override fun getHighlighter(): SyntaxHighlighter = D2SyntaxHighlighter()

    override fun getDemoText(): String {
        return """
            # This is a comment
            direction: down
            
            x -> y: Connection Label
            
            server: Server {
              shape: rectangle
              style.fill: "#7D9FC5"
              style.opacity: 0.8
            }
            
            client -> server: Request {
              style.animated: true
            }
            
            server -> database: Query
            
            database: {
              shape: cylinder
              width: 100
              height: 150
            }
        """.trimIndent()
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return DESCRIPTORS
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    override fun getDisplayName(): String = "D2"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", D2SyntaxHighlighter.KEYWORD_KEYS[0]),
            AttributesDescriptor("Identifier", D2SyntaxHighlighter.IDENTIFIER_KEYS[0]),
            AttributesDescriptor("Number", D2SyntaxHighlighter.NUMBER_KEYS[0]),
            AttributesDescriptor("String", D2SyntaxHighlighter.STRING_KEYS[0]),
            AttributesDescriptor("Colon", D2SyntaxHighlighter.COLON_KEYS[0]),
            AttributesDescriptor("Comment", D2SyntaxHighlighter.COMMENT_KEYS[0]),
            AttributesDescriptor("Operator//Arrow", D2SyntaxHighlighter.OPERATOR_KEYS[0])
        )
    }
}
