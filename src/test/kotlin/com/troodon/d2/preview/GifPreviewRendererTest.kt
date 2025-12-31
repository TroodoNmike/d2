package com.troodon.d2.preview

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertNotNull

class GifPreviewRendererTest : BasePlatformTestCase() {

    private lateinit var renderer: GifPreviewRenderer

    override fun setUp() {
        super.setUp()
        renderer = GifPreviewRenderer(project)
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
    fun `test getFileExtension returns png`() {
        assertEquals(".png", renderer.getFileExtension())
    }

    @Test
    fun `test render with valid PNG file`() {
        // Create a temporary PNG file
        val tempFile = createTempFile("test", ".png")
        try {
            val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
            ImageIO.write(image, "png", tempFile)

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
}
