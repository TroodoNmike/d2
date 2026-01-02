package com.troodon.d2.editor

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class D2IdentifierCompletionProviderTest : BasePlatformTestCase() {

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

    fun testExcludeCurrentNodeFromCompletion() {
        myFixture.configureByText("test.d2", """
            anything: {
              label: "Anything"
            }

            expense: {
              <caret>
            }

            other: {
              label: "Other"
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupStrings = myFixture.lookupElementStrings

        assertNotNull(lookupStrings)
        assertTrue(lookupStrings!!.contains("anything"))
        assertTrue(lookupStrings.contains("other"))
        // The current node "expense" should NOT be in the completion list
        assertFalse(lookupStrings.contains("expense"))
    }
}
