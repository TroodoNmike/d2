package com.troodon.d2.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SvgSanitizerTest {

    @Test
    fun `strips script tags with content`() {
        val input = """<svg><script>alert(1)</script><rect/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("<script"))
        assertFalse(result.contains("alert"))
        assertTrue(result.contains("<rect/>"))
    }

    @Test
    fun `strips self-closing script tags`() {
        val input = """<svg><script src="evil.js"/><rect/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("<script"))
        assertFalse(result.contains("evil.js"))
    }

    @Test
    fun `strips script tags case-insensitively`() {
        val input = """<svg><SCRIPT>alert(1)</SCRIPT><Script>x</Script></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.lowercase().contains("script"))
        assertFalse(result.contains("alert"))
    }

    @Test
    fun `strips iframe tags`() {
        val input = """<svg><iframe src="evil.html"></iframe><rect/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("<iframe"))
        assertTrue(result.contains("<rect/>"))
    }

    @Test
    fun `preserves foreignObject so Markdown blocks can render`() {
        // D2 renders |md ...| blocks as <foreignObject> containing XHTML.
        // It must survive this first pass; DOMPurify sanitizes its contents
        // in-browser. Stripping it here would make Markdown disappear.
        val input = """<svg><foreignObject><div xmlns="http://www.w3.org/1999/xhtml"><p>rendered markdown</p></div></foreignObject><rect/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertTrue(result.contains("<foreignObject"))
        assertTrue(result.contains("rendered markdown"))
        assertTrue(result.contains("<rect/>"))
    }

    @Test
    fun `strips dangerous content inside foreignObject`() {
        val input = """<svg><foreignObject><div xmlns="http://www.w3.org/1999/xhtml"><p>safe</p><script>alert(1)</script><a onclick="evil()" href="javascript:evil()">x</a></div></foreignObject></svg>"""
        val result = SvgSanitizer.sanitize(input)
        // Wrapper and safe content kept...
        assertTrue(result.contains("<foreignObject"))
        assertTrue(result.contains("safe"))
        // ...but the dangerous bits inside are still removed.
        assertFalse(result.contains("<script"))
        assertFalse(result.contains("alert"))
        assertFalse(result.contains("onclick"))
        assertFalse(result.contains("javascript:"))
    }

    @Test
    fun `strips onload event handler`() {
        val input = """<svg onload="alert(1)"><rect/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("onload"))
        assertFalse(result.contains("alert"))
        assertTrue(result.contains("<rect/>"))
    }

    @Test
    fun `strips onerror event handler`() {
        val input = """<svg><image onerror="alert(1)"/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("onerror"))
    }

    @Test
    fun `strips onclick event handler`() {
        val input = """<svg><rect onclick="doEvil()"/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("onclick"))
        assertFalse(result.contains("doEvil"))
    }

    @Test
    fun `strips javascript protocol in href`() {
        val input = """<svg><a href="javascript:alert(1)"><text>click</text></a></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("javascript:"))
        assertTrue(result.contains("""href="#""""))
    }

    @Test
    fun `strips javascript protocol in xlink href`() {
        val input = """<svg><a xlink:href="javascript:alert(1)"><text>click</text></a></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("javascript:"))
        assertTrue(result.contains("""xlink:href="#""""))
    }

    @Test
    fun `preserves normal href values`() {
        val input = """<svg><a href="https://example.com"><text>link</text></a></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertTrue(result.contains("""href="https://example.com""""))
    }

    @Test
    fun `preserves xlink href internal references`() {
        val input = """<svg><use xlink:href="#myId"/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertTrue(result.contains("""xlink:href="#myId""""))
    }

    @Test
    fun `preserves style elements`() {
        val input = """<svg><style>.cls { fill: red; }</style><rect class="cls"/></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertTrue(result.contains("<style>"))
        assertTrue(result.contains("fill: red"))
    }

    @Test
    fun `preserves standard SVG elements`() {
        val input = """<svg viewBox="0 0 100 100"><g><path d="M0,0"/><rect width="10" height="10"/><circle cx="5" cy="5" r="3"/><text>hello</text><defs><clipPath id="c"/></defs></g></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertEquals(input, result)
    }

    @Test
    fun `handles typical D2 SVG output unchanged`() {
        val input = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 446 266"><style>
.fill-N7{fill:#FFFFFF;}.fill-B6{fill:#1269D3;}.stroke-B1{stroke:#0D32B2;}
</style><rect x="0" y="0" width="446" height="266" fill="#FFFFFF" class="fill-N7"/><g><rect x="12" y="12" width="120" height="66" rx="5" ry="5" class="fill-B6"/><text x="72" y="49" class="fill-N7">Hello</text></g></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertEquals(input, result)
    }

    @Test
    fun `strips multiple dangerous elements in one pass`() {
        val input = """<svg><script>bad1</script><rect onload="bad2"/><a href="javascript:bad3"><foreignObject><script>bad4</script></foreignObject></a></svg>"""
        val result = SvgSanitizer.sanitize(input)
        assertFalse(result.contains("bad1"))
        assertFalse(result.contains("bad2"))
        assertFalse(result.contains("bad3"))
        assertFalse(result.contains("bad4"))
        // foreignObject wrapper itself is preserved (its dangerous child is not).
        assertTrue(result.contains("<foreignObject"))
    }
}
