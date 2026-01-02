package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class D2CompletionContributorTest : BasePlatformTestCase() {

    fun testCompletionForDefinedIdentifiers() {
        myFixture.configureByText("test.d2", """
            anything: {
              label: "Anything"
            }

            expenseElse: {
              label: "Expense Else"
            }

            expense: {
              label: "Monthly cost"
            }

            e<caret>
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("expense"))
        assertTrue(lookupStrings.contains("expenseElse"))
    }

    fun testCompletionIncludesAllIdentifiers() {
        myFixture.configureByText("test.d2", """
            anything: {
              label: "Anything"
            }

            expenseElse: {
              label: "Expense Else"
            }

            expense: {
              label: "Monthly cost"
            }

            <caret>
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("anything"))
        assertTrue(lookupStrings.contains("expense"))
        assertTrue(lookupStrings.contains("expenseElse"))
    }

    fun testCompletionForConnectionIdentifiers() {
        myFixture.configureByText("test.d2", """
            one -> two
            
            <caret>
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("one"))
        assertTrue(lookupStrings.contains("two"))
    }

    fun testCompletionForMixedIdentifiers() {
        myFixture.configureByText("test.d2", """
            anything: {
              label: "Anything"
            }

            expense: {
              label: "Monthly cost"
            }

            one -> two
            anything -> expense

            <caret>
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("anything"))
        assertTrue(lookupStrings.contains("expense"))
        assertTrue(lookupStrings.contains("one"))
        assertTrue(lookupStrings.contains("two"))
    }

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
}
