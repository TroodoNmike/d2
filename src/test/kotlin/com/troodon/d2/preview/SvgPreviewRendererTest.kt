package com.troodon.d2.preview

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import java.io.File
import kotlin.test.assertNotNull

class SvgPreviewRendererTest : BasePlatformTestCase() {

    private lateinit var renderer: SvgPreviewRenderer

    override fun setUp() {
        super.setUp()
        renderer = SvgPreviewRenderer(project)
    }

    override fun tearDown() {
        try {
            renderer.dispose()
        } finally {
            super.tearDown()
        }
    }

    @Test
    fun `test getComponent returns non-null component`() {
        val component = renderer.getComponent()
        assertNotNull(component)
    }

    @Test
    fun `test getFileExtension returns svg`() {
        assertEquals(".svg", renderer.getFileExtension())
    }

    @Test
    fun `test render with valid SVG file`() {
        // Create a temporary SVG file
        val tempFile = createTempFile("test", ".svg")
        try {
            val svgContent = """
                <svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
                    <rect width="100" height="100" fill="blue"/>
                </svg>
            """.trimIndent()
            tempFile.writeText(svgContent)

            // Create a temporary source file
            val sourceFile = createTempFile("source", ".d2")
            try {
                sourceFile.writeText("test: diagram")

                // Test rendering
                renderer.render(sourceFile, tempFile)

                // Verify component is still valid
                assertNotNull(renderer.getComponent())
            } finally {
                sourceFile.delete()
            }
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `test render with non-existent file`() {
        val nonExistentFile = File("/tmp/non_existent_${System.currentTimeMillis()}.svg")
        val sourceFile = createTempFile("source", ".d2")
        try {
            sourceFile.writeText("test: diagram")

            // Should not throw, just handle gracefully
            renderer.render(sourceFile, nonExistentFile)

            // Verify component is still valid
            assertNotNull(renderer.getComponent())
        } finally {
            sourceFile.delete()
        }
    }

    @Test
    fun `test zoomIn does not throw exception`() {
        // Should not throw
        renderer.zoomIn()
    }

    @Test
    fun `test zoomOut does not throw exception`() {
        // Should not throw
        renderer.zoomOut()
    }

    @Test
    fun `test multiple zoom operations`() {
        // Should not throw
        renderer.zoomIn()
        renderer.zoomIn()
        renderer.zoomOut()
        renderer.zoomIn()
        renderer.zoomOut()
        renderer.zoomOut()
    }

    @Test
    fun `test dispose does not throw exception`() {
        // Should not throw
        renderer.dispose()
    }

    @Test
    fun `test render with empty SVG content`() {
        val tempFile = createTempFile("test", ".svg")
        try {
            tempFile.writeText("")

            val sourceFile = createTempFile("source", ".d2")
            try {
                sourceFile.writeText("test: diagram")

                // Should handle empty content gracefully
                renderer.render(sourceFile, tempFile)

                assertNotNull(renderer.getComponent())
            } finally {
                sourceFile.delete()
            }
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `test render with malformed SVG`() {
        val tempFile = createTempFile("test", ".svg")
        try {
            tempFile.writeText("<svg>malformed</not-closed>")

            val sourceFile = createTempFile("source", ".d2")
            try {
                sourceFile.writeText("test: diagram")

                // Should handle malformed SVG gracefully
                renderer.render(sourceFile, tempFile)

                assertNotNull(renderer.getComponent())
            } finally {
                sourceFile.delete()
            }
        } finally {
            tempFile.delete()
        }
    }
}
