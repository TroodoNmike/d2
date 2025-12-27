package com.troodon.d2.settings

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class D2CliValidatorTest {

    @Test
    fun `test validation result with successful installation`() {
        val result = D2CliValidator.ValidationResult(
            isInstalled = true,
            version = "v0.6.0",
            foundPath = "/usr/local/bin/d2"
        )

        assertTrue(result.isInstalled)
        assertEquals("v0.6.0", result.version)
        assertEquals(null, result.error)
        assertEquals("/usr/local/bin/d2", result.foundPath)
    }

    @Test
    fun `test validation result with installation error`() {
        val result = D2CliValidator.ValidationResult(
            isInstalled = false,
            error = "D2 not found in PATH"
        )

        assertFalse(result.isInstalled)
        assertEquals(null, result.version)
        assertEquals("D2 not found in PATH", result.error)
    }

    @Test
    fun `test validateInstallation with non-existent command`() {
        val result = D2CliValidator.validateInstallation("non-existent-d2-command-12345")

        assertFalse(result.isInstalled)
        assertNotNull(result.error)
    }

    @Test
    fun `test validateInstallation with invalid path`() {
        val result = D2CliValidator.validateInstallation("/invalid/path/to/d2")

        assertFalse(result.isInstalled)
        assertNotNull(result.error)
    }

    @Test
    fun `test validateInstallation with empty path searches common locations`() {
        val result = D2CliValidator.validateInstallation("")

        // Should attempt to search common paths and either find D2 or return appropriate error
        if (result.isInstalled) {
            assertNotNull(result.version)
            assertNotNull(result.foundPath)
        } else {
            assertNotNull(result.error)
        }
    }

    @Test
    fun `test validation result includes foundPath`() {
        val result = D2CliValidator.ValidationResult(
            isInstalled = true,
            version = "v0.6.0",
            error = null,
            foundPath = "/opt/homebrew/bin/d2"
        )

        assertTrue(result.isInstalled)
        assertEquals("/opt/homebrew/bin/d2", result.foundPath)
    }

    @Test
    fun `test validation result data class properties`() {
        val result1 = D2CliValidator.ValidationResult(true, "v1.0.0", null, "/usr/bin/d2")
        val result2 = D2CliValidator.ValidationResult(true, "v1.0.0", null, "/usr/bin/d2")
        val result3 = D2CliValidator.ValidationResult(false, null, "Error", null)

        // Test equality
        assertEquals(result1, result2)
        assertFalse(result1 == result3)

        // Test copy
        val copied = result1.copy(isInstalled = false)
        assertFalse(copied.isInstalled)
        assertEquals("v1.0.0", copied.version)
        assertEquals("/usr/bin/d2", copied.foundPath)
    }
}
