package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class D2ShapeCompletionProviderTest : BasePlatformTestCase() {

    fun testCompletionForShapeValuesInline() {
        myFixture.configureByText("test.d2", """
            one.shape: <caret>
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("rectangle"))
        assertTrue(lookupStrings.contains("circle"))
        assertTrue(lookupStrings.contains("diamond"))
        assertTrue(lookupStrings.contains("hexagon"))
    }

    fun testCompletionForShapeValuesInBlock() {
        myFixture.configureByText("test.d2", """
            node: {
              shape: <caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("rectangle"))
        assertTrue(lookupStrings.contains("square"))
        assertTrue(lookupStrings.contains("cylinder"))
        assertTrue(lookupStrings.contains("c4-person"))
    }

    fun testCompletionIncludesAllShapeValues() {
        myFixture.configureByText("test.d2", """
            node.shape: <caret>
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        val expectedShapes = listOf(
            "rectangle", "square", "page", "parallelogram", "document",
            "cylinder", "queue", "package", "step", "callout",
            "stored_data", "person", "diamond", "oval", "circle",
            "hexagon", "cloud", "c4-person"
        )
        expectedShapes.forEach { shape ->
            assertTrue("Shape '$shape' should be in completion list", lookupStrings!!.contains(shape))
        }
    }

    fun testNoShapeCompletionOutsideContext() {
        myFixture.configureByText("test.d2", """
            node: {
              label: <caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        // Should not suggest shape values when not after "shape:"
        assertFalse(lookupStrings?.contains("rectangle") == true && 
                   lookupStrings.contains("circle") && 
                   lookupStrings.contains("diamond"))
    }
}
