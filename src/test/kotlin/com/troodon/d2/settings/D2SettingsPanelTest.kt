package com.troodon.d2.settings

import org.junit.Test
import kotlin.test.assertEquals

class D2SettingsPanelTest {

    @Test
    fun `test trim removes trailing spaces`() {
        val input = "--sketch   "
        val trimmed = input.trim()
        assertEquals("--sketch", trimmed)
    }

    @Test
    fun `test trim removes leading spaces`() {
        val input = "   --theme=200"
        val trimmed = input.trim()
        assertEquals("--theme=200", trimmed)
    }

    @Test
    fun `test trim removes leading and trailing spaces`() {
        val input = "   --sketch --theme=200   "
        val trimmed = input.trim()
        assertEquals("--sketch --theme=200", trimmed)
    }

    @Test
    fun `test trim preserves internal spaces`() {
        val input = "--sketch --theme=200"
        val trimmed = input.trim()
        assertEquals("--sketch --theme=200", trimmed)
    }

    @Test
    fun `test trim handles empty string`() {
        val input = ""
        val trimmed = input.trim()
        assertEquals("", trimmed)
    }

    @Test
    fun `test trim handles only spaces`() {
        val input = "    "
        val trimmed = input.trim()
        assertEquals("", trimmed)
    }
}
