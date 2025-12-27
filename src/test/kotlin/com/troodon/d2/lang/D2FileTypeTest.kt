package com.troodon.d2.lang

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class D2FileTypeTest {

    @Test
    fun `test file type name`() {
        val fileType = D2FileType()
        assertEquals("D2", fileType.name)
    }

    @Test
    fun `test file type description`() {
        val fileType = D2FileType()
        assertEquals("D2 diagram file", fileType.description)
    }

    @Test
    fun `test default extension`() {
        val fileType = D2FileType()
        assertEquals("d2", fileType.defaultExtension)
    }

    @Test
    fun `test language association`() {
        val fileType = D2FileType()
        assertNotNull(fileType.language)
        assertEquals(D2Language.INSTANCE, fileType.language)
    }
}
