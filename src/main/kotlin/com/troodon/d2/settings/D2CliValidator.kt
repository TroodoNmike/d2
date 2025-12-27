package com.troodon.d2.settings

import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.util.concurrent.TimeUnit

object D2CliValidator {

    private val LOG = Logger.getInstance(D2CliValidator::class.java)

    data class ValidationResult(
        val isInstalled: Boolean,
        val version: String? = null,
        val error: String? = null,
        val foundPath: String? = null
    )

    /**
     * Returns common D2 CLI installation paths based on the operating system
     */
    private fun getCommonD2Paths(): List<String> {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        return when {
            // macOS
            os.contains("mac") -> listOf(
                "d2", // Check PATH first
                "/usr/local/bin/d2",
                "/opt/homebrew/bin/d2",
                "/opt/local/bin/d2",
                "$userHome/.local/bin/d2",
                "$userHome/go/bin/d2"
            )
            // Linux
            os.contains("nix") || os.contains("nux") || os.contains("aix") -> listOf(
                "d2", // Check PATH first
                "/usr/local/bin/d2",
                "/usr/bin/d2",
                "/opt/d2/bin/d2",
                "$userHome/.local/bin/d2",
                "$userHome/go/bin/d2",
                "/snap/bin/d2"
            )
            // Windows
            os.contains("win") -> listOf(
                "d2", // Check PATH first
                "d2.exe",
                """C:\Program Files\D2\d2.exe""",
                """C:\Program Files (x86)\D2\d2.exe""",
                """$userHome\go\bin\d2.exe""",
                """$userHome\.local\bin\d2.exe""",
                """C:\tools\d2.exe"""
            )
            else -> listOf("d2")
        }
    }

    fun validateInstallation(d2CliPath: String = ""): ValidationResult {
        // If a specific path is provided, validate only that path
        if (d2CliPath.isNotBlank()) {
            return tryValidatePath(d2CliPath)
        }

        // If path is empty, try common paths
        val pathsToTry = getCommonD2Paths()

        for (path in pathsToTry) {
            // Skip if file doesn't exist (except for "d2" which might be in PATH)
            if (path != "d2" && path != "d2.exe" && !File(path).exists()) {
                continue
            }

            val result = tryValidatePath(path)
            if (result.isInstalled) {
                LOG.info("Found D2 CLI at: $path")
                return result.copy(foundPath = path)
            }
        }

        // None of the paths worked
        return ValidationResult(
            isInstalled = false,
            error = "D2 CLI not found in PATH or common installation directories"
        )
    }

    private fun tryValidatePath(path: String): ValidationResult {
        return try {
            val process = ProcessBuilder(path, "--version")
                .redirectErrorStream(true)
                .start()

            val completed = process.waitFor(5, TimeUnit.SECONDS)

            if (!completed) {
                process.destroyForcibly()
                return ValidationResult(
                    isInstalled = false,
                    error = "Command timed out"
                )
            }

            if (process.exitValue() == 0) {
                val output = process.inputStream.bufferedReader().readText().trim()
                // D2 version output format: "v0.6.0" or similar
                val version = output.lines().firstOrNull { it.isNotBlank() } ?: "Unknown"

                ValidationResult(
                    isInstalled = true,
                    version = version,
                    foundPath = path
                )
            } else {
                val error = process.inputStream.bufferedReader().readText().trim()
                ValidationResult(
                    isInstalled = false,
                    error = error.takeIf { it.isNotEmpty() } ?: "Command failed"
                )
            }
        } catch (e: Exception) {
            ValidationResult(
                isInstalled = false,
                error = when (e) {
                    is java.io.IOException -> "Not found"
                    else -> e.message ?: "Unknown error"
                }
            )
        }
    }
}
