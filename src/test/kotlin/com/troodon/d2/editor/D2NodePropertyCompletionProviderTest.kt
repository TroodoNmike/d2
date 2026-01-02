package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class D2NodePropertyCompletionProviderTest : BasePlatformTestCase() {

    fun testCompletionInsideNodeBlock() {
        myFixture.configureByText("test.d2", """
            node: {
              <caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("shape"))
        assertTrue(lookupStrings.contains("icon"))
        assertTrue(lookupStrings.contains("style"))
        assertTrue(lookupStrings.contains("label"))
    }

    fun testCompletionInsideNodeBlockPartialProperty() {
        myFixture.configureByText("test.d2", """
            expense: {
              l<caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        // Should suggest "label" when typing "l"
        assertTrue("label should be in completion list", lookupStrings!!.contains("label"))
    }

    fun testNoNodePropertyCompletionOutsideBlock() {
        myFixture.configureByText("test.d2", """
            <caret>
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        // Should not suggest node properties outside a node block
        if (lookupStrings != null) {
            // If there are suggestions, they shouldn't all be node properties
            val hasAllNodeProps = lookupStrings.contains("shape") && 
                                 lookupStrings.contains("icon") && 
                                 lookupStrings.contains("style") && 
                                 lookupStrings.contains("label")
            assertFalse(hasAllNodeProps)
        }
    }

    fun testNodePropertyCompletionWithExistingProperties() {
        myFixture.configureByText("test.d2", """
            myNode: {
              label: "My Node"
              <caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        // Other properties should be available
        assertTrue(lookupStrings!!.contains("shape"))
        assertTrue(lookupStrings.contains("icon"))
        assertTrue(lookupStrings.contains("style"))
        // But "label" should NOT be suggested since it's already defined
        assertFalse(lookupStrings.contains("label"))
    }

    fun testExcludeMultipleDefinedProperties() {
        myFixture.configureByText("test.d2", """
            myNode: {
              label: "My Node"
              shape: rectangle
              <caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        // Only undefined properties should be suggested
        assertTrue(lookupStrings!!.contains("icon"))
        assertTrue(lookupStrings.contains("style"))
        // Already defined properties should NOT be suggested
        assertFalse(lookupStrings.contains("label"))
        assertFalse(lookupStrings.contains("shape"))
    }
}
