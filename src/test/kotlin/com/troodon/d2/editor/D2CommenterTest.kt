package com.troodon.d2.editor

import com.intellij.lang.LanguageCommenters
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.troodon.d2.lang.D2Language
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class D2CommenterTest : BasePlatformTestCase() {

    @Test
    fun testCommenterRegistered() {
        val commenter = LanguageCommenters.INSTANCE.forLanguage(D2Language.INSTANCE)
        assertNotNull(commenter, "Commenter should be registered for D2 language")
        assertEquals("# ", commenter.lineCommentPrefix)
        assertEquals("\"\"\"", commenter.blockCommentPrefix)
        assertEquals("\"\"\"", commenter.blockCommentSuffix)
    }
}
