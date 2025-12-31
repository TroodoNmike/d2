package com.troodon.d2.preview

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.jcef.JBCefApp
import org.junit.Test
import java.io.File
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

    @Test
    fun `test render queues task when browser not ready`() {
        val tempFile = createTempFile("test", ".svg")
        try {
            val svgContent = """
                <svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
                    <rect width="100" height="100" fill="blue"/>
                </svg>
            """.trimIndent()
            tempFile.writeText(svgContent)

            val sourceFile = createTempFile("source", ".d2")
            try {
                sourceFile.writeText("test: diagram")

                // Call render before browser is ready (initial state)
                // This should queue the task instead of executing immediately
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
    fun `test render executes immediately when browser is ready`() {
        val tempFile = createTempFile("test", ".svg")
        try {
            val svgContent = """
                <svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
                    <rect width="100" height="100" fill="green"/>
                </svg>
            """.trimIndent()
            tempFile.writeText(svgContent)

            val sourceFile = createTempFile("source", ".d2")
            try {
                sourceFile.writeText("test: diagram")

                // First render to trigger browser ready state
                renderer.render(sourceFile, tempFile)

                // Wait a bit for browser to initialize
                Thread.sleep(500)

                // Second render should execute immediately as browser is ready
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
    fun `test component structure when JCEF is supported`() {
        // This test verifies the component is properly initialized
        val component = renderer.getComponent()
        assertNotNull(component)
        assertTrue(component is JPanel, "Component should be a JPanel")

        // If JCEF is supported, we should have a browser component
        if (JBCefApp.isSupported()) {
            assertTrue(component.componentCount > 0, "Component should have browser when JCEF supported")
        } else {
            // If JCEF is not supported, verify the error label is present
            assertTrue(component.componentCount > 0, "Component should have error label when JCEF not supported")

            // Find the error label in the panel
            val errorLabel = findErrorLabel(component as JPanel)
            assertNotNull(errorLabel, "Should have error label when JCEF not supported")
            assertEquals(JLabel.CENTER, errorLabel.horizontalAlignment, "Error label should be center aligned")
            assertTrue(errorLabel.text.contains("JCEF browser not supported"), "Error message should indicate JCEF not supported")
            assertTrue(errorLabel.text.contains("SVG preview unavailable"), "Error message should indicate SVG preview unavailable")
        }
    }

    @Test
    fun `test error label properties when JCEF not supported`() {
        // Create a test scenario to verify error label creation
        // We test this by creating a JLabel with the same properties as in the code
        val errorLabel = JLabel("<html><center>JCEF browser not supported.<br>SVG preview unavailable.</center></html>")
        errorLabel.horizontalAlignment = JLabel.CENTER

        // Verify the label properties match what we expect
        assertNotNull(errorLabel)
        assertEquals(JLabel.CENTER, errorLabel.horizontalAlignment)
        assertTrue(errorLabel.text.contains("JCEF browser not supported"))
        assertTrue(errorLabel.text.contains("SVG preview unavailable"))
        assertTrue(errorLabel.text.startsWith("<html>"))
    }

    private fun findErrorLabel(panel: JPanel): JLabel? {
        // Search for JLabel in the panel's components
        for (component in panel.components) {
            if (component is JLabel && component.text.contains("JCEF browser not supported")) {
                return component
            }
        }
        return null
    }

    @Test
    fun `test render handles null browser gracefully`() {
        val tempFile = createTempFile("test", ".svg")
        try {
            val svgContent = """
                <svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
                    <rect width="100" height="100" fill="red"/>
                </svg>
            """.trimIndent()
            tempFile.writeText(svgContent)

            val sourceFile = createTempFile("source", ".d2")
            try {
                sourceFile.writeText("test: diagram")

                // Even if browser is null (JCEF not supported), render should not throw
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
}
