package com.troodon.d2.markdown

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertContains

class D2RunnerTest {

    private val originalRunner = D2Runner.processRunner

    @After
    fun restoreRunner() {
        D2Runner.processRunner = originalRunner
    }

    @Test
    fun `renderToSvg returns stdout on success`() {
        val fakeSvg = "<svg xmlns='http://www.w3.org/2000/svg'><rect width='100' height='100'/></svg>"
        D2Runner.processRunner = { _, _ -> Pair(0, fakeSvg) }

        val result = D2Runner.renderToSvg("x -> y", "/fake/d2")

        assertEquals(fakeSvg, result)
    }

    @Test
    fun `renderToSvg passes source as stdin`() {
        val capturedStdin = mutableListOf<String>()
        D2Runner.processRunner = { _, stdin ->
            capturedStdin.add(stdin)
            Pair(0, "<svg/>")
        }

        D2Runner.renderToSvg("a -> b: hello", "/fake/d2")

        assertEquals(listOf("a -> b: hello"), capturedStdin)
    }

    @Test
    fun `renderToSvg passes correct command`() {
        val capturedCmd = mutableListOf<List<String>>()
        D2Runner.processRunner = { cmd, _ ->
            capturedCmd.add(cmd)
            Pair(0, "<svg/>")
        }

        D2Runner.renderToSvg("x -> y", "/usr/local/bin/d2")

        assertEquals(listOf(listOf("/usr/local/bin/d2", "-", "-")), capturedCmd)
    }

    @Test
    fun `renderToSvg throws on non-zero exit code`() {
        D2Runner.processRunner = { _, _ -> Pair(1, "parse error at line 3") }

        val ex = assertFailsWith<RuntimeException> {
            D2Runner.renderToSvg("invalid!!!", "/fake/d2")
        }
        assertTrue("Exception should mention exit code", ex.message!!.contains("1"))
    }

    @Test
    fun `renderToSvg includes process output in exception message`() {
        val errorOutput = "D2 compilation error: unexpected token 'invalid'"
        D2Runner.processRunner = { _, _ -> Pair(1, errorOutput) }

        val ex = assertFailsWith<RuntimeException> {
            D2Runner.renderToSvg("invalid!!!", "/fake/d2")
        }
        assertTrue(ex.message!!.contains(errorOutput))
    }

    @Test
    fun `renderToSvg includes d2Arguments in command`() {
        val capturedCmd = mutableListOf<List<String>>()
        D2Runner.processRunner = { cmd, _ -> capturedCmd.add(cmd); Pair(0, "<svg/>") }

        D2Runner.renderToSvg("x -> y", "/usr/bin/d2", d2Arguments = "--sketch --theme=200")

        assertEquals(listOf("/usr/bin/d2", "--sketch", "--theme=200", "-", "-"), capturedCmd.first())
    }

    @Test
    fun `renderToSvg wraps command with wsl when useWsl is true`() {
        val capturedCmd = mutableListOf<List<String>>()
        D2Runner.processRunner = { cmd, _ -> capturedCmd.add(cmd); Pair(0, "<svg/>") }

        D2Runner.renderToSvg("x -> y", "/usr/bin/d2", useWsl = true, wslDistro = "Ubuntu")

        val cmd = capturedCmd.first()
        assertEquals("wsl.exe", cmd[0])
        assertContains(cmd, "/usr/bin/d2")
    }

    @Test
    fun `renderToSvg truncates very long error output`() {
        val longOutput = "x".repeat(1000)
        D2Runner.processRunner = { _, _ -> Pair(1, longOutput) }

        val ex = assertFailsWith<RuntimeException> {
            D2Runner.renderToSvg("x", "/fake/d2")
        }
        // Error message should not exceed ~550 chars (500 cap + boilerplate)
        assertTrue(ex.message!!.length < 600)
    }
}
