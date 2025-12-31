package com.troodon.d2.settings

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class D2SettingsStateTest {

    @Test
    fun `test default d2 CLI path is empty`() {
        val state = D2SettingsState()
        assertEquals("", state.d2CliPath)
    }

    @Test
    fun `test default d2Arguments has animate-interval`() {
        val state = D2SettingsState()
        assertEquals(DEFAULT_D2_ARGUMENTS, state.d2Arguments)
    }

    @Test
    fun `test getState returns self`() {
        val state = D2SettingsState()
        val returnedState = state.state

        assertNotNull(returnedState)
        assertEquals(state, returnedState)
    }

    @Test
    fun `test loadState copies values`() {
        val state1 = D2SettingsState()
        state1.d2CliPath = "/custom/path/to/d2"

        val state2 = D2SettingsState()
        state2.loadState(state1)

        assertEquals("/custom/path/to/d2", state2.d2CliPath)
    }

    @Test
    fun `test setting custom d2 CLI path`() {
        val state = D2SettingsState()
        state.d2CliPath = "/usr/local/bin/d2"

        assertEquals("/usr/local/bin/d2", state.d2CliPath)
    }

    @Test
    fun `test loadState with different path`() {
        val sourceState = D2SettingsState()
        sourceState.d2CliPath = "/new/path/d2"

        val targetState = D2SettingsState()
        assertEquals("", targetState.d2CliPath)

        targetState.loadState(sourceState)
        assertEquals("/new/path/d2", targetState.d2CliPath)
    }

    @Test
    fun `test getEffectiveD2Path returns configured path when set`() {
        val state = D2SettingsState()
        state.d2CliPath = "/custom/path/d2"

        assertEquals("/custom/path/d2", state.getEffectiveD2Path())
    }

    @Test
    fun `test getEffectiveD2Path searches common locations when empty`() {
        val state = D2SettingsState()
        state.d2CliPath = ""

        val effectivePath = state.getEffectiveD2Path()
        assertNotNull(effectivePath)
        // Should return either a found path or "d2" as fallback
        assert(effectivePath.isNotEmpty())
    }
}
