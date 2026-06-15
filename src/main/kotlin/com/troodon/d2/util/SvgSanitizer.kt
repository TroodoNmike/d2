package com.troodon.d2.util

/**
 * First-pass, string-level sanitizer for SVG content before it is handed to a
 * JCEF browser panel. This is a defense-in-depth layer: the authoritative
 * sanitization happens in-browser via DOMPurify (see [com.troodon.d2.preview.SvgPreviewRenderer]),
 * which parses the markup with the same engine that renders it.
 *
 * Strips dangerous elements (`<script>`, `<iframe>`), event handler attributes
 * (`on*`), and the `javascript:` protocol in hrefs. Preserves `<style>` elements,
 * which D2 uses extensively for styling.
 *
 * `<foreignObject>` is intentionally NOT stripped here: D2 renders Markdown
 * blocks (`|md ... |`) as `<foreignObject>` containing XHTML, so removing it
 * would make Markdown disappear. Its contents are sanitized in-browser by
 * DOMPurify instead.
 */
object SvgSanitizer {

    // Elements to strip entirely (including content).
    // Case-insensitive, handles both paired tags and self-closing tags.
    private val DANGEROUS_ELEMENTS = listOf("script", "iframe")

    // Matches on* event handler attributes with double- or single-quoted values.
    private val EVENT_HANDLER_ATTR = Regex(
        """\s+on\w+\s*=\s*(?:"[^"]*"|'[^']*')""",
        RegexOption.IGNORE_CASE
    )

    // Matches javascript: protocol in href or xlink:href (double-quoted).
    private val JS_HREF_DOUBLE = Regex(
        """((?:xlink:)?href)\s*=\s*"javascript:[^"]*"""",
        RegexOption.IGNORE_CASE
    )

    // Matches javascript: protocol in href or xlink:href (single-quoted).
    private val JS_HREF_SINGLE = Regex(
        """((?:xlink:)?href)\s*=\s*'javascript:[^']*'""",
        RegexOption.IGNORE_CASE
    )

    fun sanitize(svg: String): String {
        var result = svg

        // Strip dangerous elements (paired and self-closing)
        for (tag in DANGEROUS_ELEMENTS) {
            // Paired: <script ...>...</script>
            result = result.replace(
                Regex("""<$tag\b[^>]*>[\s\S]*?</$tag\s*>""", RegexOption.IGNORE_CASE),
                ""
            )
            // Self-closing: <script ... />
            result = result.replace(
                Regex("""<$tag\b[^>]*/\s*>""", RegexOption.IGNORE_CASE),
                ""
            )
        }

        // Strip event handler attributes
        result = EVENT_HANDLER_ATTR.replace(result, "")

        // Replace javascript: hrefs with safe "#"
        result = JS_HREF_DOUBLE.replace(result) { match ->
            """${match.groupValues[1]}="#""""
        }
        result = JS_HREF_SINGLE.replace(result) { match ->
            """${match.groupValues[1]}='#'"""
        }

        return result
    }
}
