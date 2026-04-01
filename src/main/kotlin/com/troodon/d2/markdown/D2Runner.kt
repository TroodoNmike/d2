package com.troodon.d2.markdown

import com.intellij.openapi.diagnostic.Logger
import com.troodon.d2.util.D2CommandBuilder
import java.util.concurrent.TimeUnit

object D2Runner {
    private val LOG = Logger.getInstance(D2Runner::class.java)
    private const val TIMEOUT_SECONDS = 10L

    /**
     * Overridable for testing. Accepts a command list and stdin string,
     * returns (exitCode, combinedOutput).
     */
    internal var processRunner: (command: List<String>, stdin: String) -> Pair<Int, String> =
        ::defaultRun

    /**
     * Renders [source] to SVG by piping it to the D2 CLI.
     * Uses stdin → stdout (`d2 [args] - -`) so no temp file is needed.
     *
     * @throws RuntimeException if D2 is not found, times out, or exits non-zero.
     */
    fun renderToSvg(
        source: String,
        d2Path: String,
        d2Arguments: String = "",
        useWsl: Boolean = false,
        wslDistro: String = ""
    ): String {
        val command = D2CommandBuilder.buildD2StdinStdoutCommand(d2Path, d2Arguments, useWsl, wslDistro)
        val (exitCode, output) = processRunner(command, source)
        if (exitCode != 0) {
            throw RuntimeException("D2 exited with code $exitCode: ${output.take(500)}")
        }
        return output
    }

    private fun defaultRun(command: List<String>, input: String): Pair<Int, String> {
        val process = ProcessBuilder(command).start()

        // Write stdin on a separate thread to prevent deadlock when the process
        // buffer fills up before we start reading stdout.
        val writerThread = Thread {
            try {
                process.outputStream.bufferedWriter().use { it.write(input) }
            } catch (_: Exception) {
                // Process may have terminated early (e.g. parse error); ignore.
            }
        }
        writerThread.start()

        // Read stdout (SVG) and stderr separately so CLI progress messages
        // on stderr don't pollute the SVG output.
        var stdout = ""
        var stderr = ""
        val stdoutThread = Thread { stdout = process.inputStream.bufferedReader().readText() }
        val stderrThread = Thread { stderr = process.errorStream.bufferedReader().readText() }
        stdoutThread.start()
        stderrThread.start()

        // Wait on the process first so the total timeout is TIMEOUT_SECONDS, not
        // TIMEOUT_SECONDS * (number of threads). Once the process exits its streams
        // reach EOF and the reader/writer threads finish almost immediately.
        val completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            throw RuntimeException("D2 timed out after ${TIMEOUT_SECONDS}s")
        }

        // Process has exited — a short safety timeout is enough.
        val safetyMs = 2_000L
        writerThread.join(safetyMs)
        stdoutThread.join(safetyMs)
        stderrThread.join(safetyMs)

        val exitCode = process.exitValue()
        if (stderr.isNotBlank()) LOG.debug("D2 stderr: $stderr")
        // On error, include stderr in the message for diagnostics.
        val output = if (exitCode != 0) stderr.ifBlank { stdout } else stdout
        return Pair(exitCode, output)
    }
}
