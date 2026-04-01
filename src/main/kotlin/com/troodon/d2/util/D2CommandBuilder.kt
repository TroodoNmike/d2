package com.troodon.d2.util

object D2CommandBuilder {

    private val ARGUMENT_SPLIT_REGEX = Regex("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")

    private fun splitArguments(d2Arguments: String): List<String> =
        if (d2Arguments.isBlank()) emptyList() else d2Arguments.split(ARGUMENT_SPLIT_REGEX)

    /**
     * Wraps a command list with `wsl.exe` when WSL mode is enabled.
     */
    fun buildCommand(d2Path: String, args: List<String>, useWsl: Boolean, wslDistro: String = ""): List<String> {
        val base = listOf(d2Path) + args
        if (!useWsl) return base

        val wsl = mutableListOf("wsl.exe")
        if (wslDistro.isNotBlank()) {
            wsl.add("-d")
            wsl.add(wslDistro)
        }
        wsl.add("-e")
        wsl.addAll(base)
        return wsl
    }

    /**
     * Converts a Windows path to a WSL path.
     * - `C:\Users\foo` -> `/mnt/c/Users/foo`
     * - `\\wsl$\Ubuntu\home\foo` -> `/home/foo`
     * - `\\wsl.localhost\Ubuntu\home\foo` -> `/home/foo`
     * - Paths with forward slashes are handled.
     */
    fun convertToWslPath(windowsPath: String): String {
        val normalized = windowsPath.replace("\\", "/")

        // UNC WSL path: //wsl$/Distro/rest or //wsl.localhost/Distro/rest
        val uncPattern = Regex("""^//wsl(?:\$|\.localhost)/[^/]+(.*)$""")
        uncPattern.matchEntire(normalized)?.let {
            return it.groupValues[1].ifEmpty { "/" }
        }

        // Drive letter path: C:/Users/foo -> /mnt/c/Users/foo
        val drivePattern = Regex("""^([A-Za-z]):/(.*)$""")
        drivePattern.matchEntire(normalized)?.let {
            val drive = it.groupValues[1].lowercase()
            val rest = it.groupValues[2]
            return "/mnt/$drive/$rest"
        }

        // Already a Unix-style path or unrecognized - return as-is
        return normalized
    }

    /**
     * Builds a full D2 render command. Uses stdin ("-") for input.
     * Output file path is converted to WSL path when WSL is enabled.
     */
    fun buildD2RenderCommand(
        d2Path: String,
        d2Arguments: String,
        outputFile: String,
        useWsl: Boolean,
        wslDistro: String = ""
    ): List<String> {
        val args = mutableListOf<String>()
        args.addAll(splitArguments(d2Arguments))
        args.add("-")
        args.add(if (useWsl) convertToWslPath(outputFile) else outputFile)
        return buildCommand(d2Path, args, useWsl, wslDistro)
    }

    /**
     * Builds a D2 fmt command. File path is converted to WSL path when WSL is enabled.
     */
    fun buildD2FmtCommand(
        d2Path: String,
        filePath: String,
        useWsl: Boolean,
        wslDistro: String = ""
    ): List<String> {
        val wslFilePath = if (useWsl) convertToWslPath(filePath) else filePath
        return buildCommand(d2Path, listOf("fmt", wslFilePath), useWsl, wslDistro)
    }

    /**
     * Builds a D2 render command that reads from stdin and writes SVG to stdout.
     * Used by the Markdown preview to avoid a temp output file.
     */
    fun buildD2StdinStdoutCommand(
        d2Path: String,
        d2Arguments: String,
        useWsl: Boolean,
        wslDistro: String = ""
    ): List<String> {
        val args = mutableListOf<String>()
        args.addAll(splitArguments(d2Arguments))
        args.add("-") // stdin
        args.add("-") // stdout (SVG)
        return buildCommand(d2Path, args, useWsl, wslDistro)
    }

    /**
     * Builds a D2 version check command.
     */
    fun buildD2VersionCommand(
        d2Path: String,
        useWsl: Boolean,
        wslDistro: String = ""
    ): List<String> {
        return buildCommand(d2Path, listOf("--version"), useWsl, wslDistro)
    }
}
