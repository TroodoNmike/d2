package com.troodon.d2.preview

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Image
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*

class PngPreviewRenderer : PreviewRenderer {

    private val imageLabel = JLabel()
    private val scrollPane = JBScrollPane(imageLabel)
    private val panel = JPanel(BorderLayout())

    private var originalImage: Image? = null
    private var zoomLevel = 1.0
    private val zoomStep = 0.1
    private val minZoom = 0.1
    private val maxZoom = 5.0

    private var dragStart: Point? = null

    init {
        panel.add(scrollPane, BorderLayout.CENTER)
        imageLabel.horizontalAlignment = JLabel.CENTER
        imageLabel.verticalAlignment = JLabel.CENTER

        setupZoomAndDrag()
    }

    override fun getComponent(): JComponent = panel

    override fun render(sourceFile: File, outputFile: File) {
        ApplicationManager.getApplication().invokeLater {
            if (outputFile.exists()) {
                val image = ImageIO.read(outputFile)
                if (image != null) {
                    imageLabel.text = null

                    // Save scroll position before updating
                    val viewport = scrollPane.viewport
                    val savedScrollX = viewport.viewPosition.x
                    val savedScrollY = viewport.viewPosition.y

                    originalImage = image

                    // Only calculate fit to screen zoom if this is the first render (zoomLevel == 1.0)
                    if (zoomLevel == 1.0) {
                        zoomLevel = calculateFitToScreenZoom(image)
                    }

                    updateZoom()

                    // Restore scroll position after a short delay
                    SwingUtilities.invokeLater {
                        viewport.viewPosition = Point(savedScrollX, savedScrollY)
                    }
                } else {
                    showError("Failed to load image")
                }
            } else {
                showError("Output file not found")
            }
        }
    }

    override fun zoomIn() {
        if (zoomLevel < maxZoom) {
            zoomLevel = (zoomLevel + zoomStep).coerceAtMost(maxZoom)
            updateZoom()
        }
    }

    override fun zoomOut() {
        if (zoomLevel > minZoom) {
            zoomLevel = (zoomLevel - zoomStep).coerceAtLeast(minZoom)
            updateZoom()
        }
    }

    override fun getFileExtension(): String = ".png"

    override fun dispose() {
        // Nothing to dispose
    }

    private fun setupZoomAndDrag() {
        // Mouse wheel zoom
        scrollPane.addMouseWheelListener { e: MouseWheelEvent ->
            if (e.isControlDown || e.isMetaDown) {
                e.consume()
                if (e.wheelRotation < 0) {
                    zoomIn()
                } else {
                    zoomOut()
                }
            }
        }

        // Drag to pan
        val mouseAdapter = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStart = Point(e.x, e.y)
                    scrollPane.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                dragStart = null
                scrollPane.cursor = Cursor.getDefaultCursor()
            }

            override fun mouseDragged(e: MouseEvent) {
                dragStart?.let { start ->
                    val viewport = scrollPane.viewport
                    val viewPosition = viewport.viewPosition

                    val deltaX = start.x - e.x
                    val deltaY = start.y - e.y

                    viewPosition.translate(deltaX, deltaY)

                    imageLabel.scrollRectToVisible(
                        java.awt.Rectangle(viewPosition, viewport.size)
                    )

                    dragStart = Point(e.x, e.y)
                }
            }
        }

        scrollPane.addMouseListener(mouseAdapter)
        scrollPane.addMouseMotionListener(mouseAdapter)
        imageLabel.addMouseListener(mouseAdapter)
        imageLabel.addMouseMotionListener(mouseAdapter)
    }

    private fun updateZoom() {
        originalImage?.let { img ->
            val width = (img.getWidth(null) * zoomLevel).toInt()
            val height = (img.getHeight(null) * zoomLevel).toInt()
            val scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
            imageLabel.icon = ImageIcon(scaledImage)
        }
    }

    private fun calculateFitToScreenZoom(image: Image): Double {
        val imageWidth = image.getWidth(null)
        val imageHeight = image.getHeight(null)

        val viewportWidth = scrollPane.viewport.width
        val viewportHeight = scrollPane.viewport.height

        // Add some padding (90% of viewport)
        val maxWidth = viewportWidth * 0.9
        val maxHeight = viewportHeight * 0.9

        if (imageWidth <= 0 || imageHeight <= 0 || viewportWidth <= 0 || viewportHeight <= 0) {
            return 1.0
        }

        val widthScale = maxWidth / imageWidth
        val heightScale = maxHeight / imageHeight

        // Use the smaller scale to ensure the entire image fits
        return minOf(widthScale, heightScale, 1.0).coerceAtLeast(minZoom)
    }

    private fun showError(message: String) {
        ApplicationManager.getApplication().invokeLater {
            imageLabel.icon = null
            imageLabel.text = "<html><body style='color: red;'>$message</body></html>"
        }
    }
}
