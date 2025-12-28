package com.troodon.d2.preview

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.Alarm
import com.intellij.util.ui.JBUI
import com.troodon.d2.settings.D2SettingsState
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.Image
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*

class D2PreviewPanel(
    private val project: Project,
    private val file: VirtualFile,
    private val editor: Editor
) : Disposable {

    private val LOG = Logger.getInstance(D2PreviewPanel::class.java)
    private val panel = JPanel(BorderLayout())
    private val imageLabel = JLabel()
    private val scrollPane = JBScrollPane(imageLabel)
    private val statusLabel = JLabel(" ")
    private val imageTypeLabel = JLabel("")
    private val refreshButton = JButton("Refresh", AllIcons.Actions.Refresh)
    private val zoomInButton = JButton("Zoom In", AllIcons.General.ZoomIn)
    private val zoomOutButton = JButton("Zoom Out", AllIcons.General.ZoomOut)
    private val exportButton = JButton("Export", AllIcons.ToolbarDecorator.Export)
    private val autoRefreshCheckBox = JCheckBox("Auto-refresh", true)
    private val autoFormatCheckBox = JCheckBox("Auto-format (d2 fmt)", false)
    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val debounceDelay = 1000 // milliseconds
    
    private var tempOutputFile: File? = null
    private var tempSourceFile: File? = null
    private var originalImage: Image? = null
    private var zoomLevel = 1.0
    private val zoomStep = 0.1
    private val minZoom = 0.1
    private val maxZoom = 5.0
    private var isFirstRender = true
    private var isFormatting = false

    // For dragging
    private var dragStart: Point? = null

    val component: JComponent get() = panel

    val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            if (autoRefreshCheckBox.isSelected && !isFormatting) {
                alarm.cancelAllRequests()
                alarm.addRequest({ updatePreview() }, debounceDelay)
            }
        }
    }

    private val fileSaveListener = object : FileDocumentManagerListener {
        override fun beforeDocumentSaving(document: com.intellij.openapi.editor.Document) {
            if (document == editor.document) {
                updatePreview()
            }
        }
    }

    init {
        // Register file save listener
        project.messageBus.connect(this).subscribe(
            FileDocumentManagerListener.TOPIC,
            fileSaveListener
        )
        // Setup buttons
        refreshButton.addActionListener {
            updatePreview()
        }
        
        zoomInButton.addActionListener {
            zoomIn()
            updateZoomDisplay()
        }
        
        zoomOutButton.addActionListener {
            zoomOut()
            updateZoomDisplay()
        }
        
        exportButton.addActionListener {
            exportToPreview()
        }

        // Setup top toolbar with zoom and export buttons
        val topToolbar = JPanel(BorderLayout())
        topToolbar.border = JBUI.Borders.empty(2, 5)

        val tipLabel = JLabel("<html>Tip: Click and drag to pan, Ctrl+scroll to zoom</html>")
        tipLabel.font = tipLabel.font.deriveFont(11f)

        val tipPanel = JPanel(BorderLayout())
        tipPanel.add(tipLabel, BorderLayout.CENTER)

        val topButtonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
        topButtonPanel.add(exportButton)
        topButtonPanel.add(zoomOutButton)
        topButtonPanel.add(zoomInButton)

        topToolbar.add(tipPanel, BorderLayout.CENTER)
        topToolbar.add(topButtonPanel, BorderLayout.EAST)

        // Setup status bar at the bottom with refresh controls
        val statusPanel = JPanel(BorderLayout())
        statusPanel.border = JBUI.Borders.empty(2, 5)
        statusLabel.font = statusLabel.font.deriveFont(11f)
        imageTypeLabel.font = imageTypeLabel.font.deriveFont(11f)

        val leftPanel = JPanel()
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        leftPanel.add(statusLabel)
        leftPanel.add(imageTypeLabel)

        val bottomButtonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
        bottomButtonPanel.add(autoFormatCheckBox)
        bottomButtonPanel.add(autoRefreshCheckBox)
        bottomButtonPanel.add(refreshButton)

        statusPanel.add(leftPanel, BorderLayout.WEST)
        statusPanel.add(bottomButtonPanel, BorderLayout.EAST)

        panel.add(topToolbar, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)
        panel.add(statusPanel, BorderLayout.SOUTH)
        imageLabel.horizontalAlignment = JLabel.CENTER
        imageLabel.verticalAlignment = JLabel.CENTER
        panel.border = JBUI.Borders.empty(10)
        
        setupZoomAndDrag()
        updatePreview() // Initial render
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
    
    private fun zoomIn() {
        if (zoomLevel < maxZoom) {
            zoomLevel = (zoomLevel + zoomStep).coerceAtMost(maxZoom)
            updateZoom()
            updateZoomDisplay()
        }
    }
    
    private fun zoomOut() {
        if (zoomLevel > minZoom) {
            zoomLevel = (zoomLevel - zoomStep).coerceAtLeast(minZoom)
            updateZoom()
            updateZoomDisplay()
        }
    }
    
    private fun updateZoom() {
        originalImage?.let { img ->
            val width = (img.getWidth(null) * zoomLevel).toInt()
            val height = (img.getHeight(null) * zoomLevel).toInt()
            val scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
            imageLabel.icon = ImageIcon(scaledImage)
        }
    }
    
    private fun updateZoomDisplay() {
        val zoomPercent = (zoomLevel * 100).toInt()
        imageTypeLabel.text = "PNG | Zoom: $zoomPercent%"
    }

    private fun updatePreview() {
        refreshButton.isEnabled = false
        showStatus("Rendering...")
        
        // Save current scroll position and zoom (only if not first render)
        val savedScrollPosition = if (isFirstRender) null else scrollPane.viewport.viewPosition
        val savedZoom = if (isFirstRender) null else zoomLevel
        
        Thread {
            try {
                // Get content from editor (unsaved changes)
                val editorContent = ApplicationManager.getApplication().runReadAction<String> {
                    editor.document.text
                }
                
                // Write editor content to a temp source file
                tempSourceFile?.delete()
                tempSourceFile = FileUtil.createTempFile("d2-source", ".d2", true)
                tempSourceFile!!.writeText(editorContent)

                // Apply d2 fmt if auto-format is enabled
                if (autoFormatCheckBox.isSelected) {
                    val d2Path = D2SettingsState.getInstance(project).getEffectiveD2Path()
                    val fmtProcess = ProcessBuilder(
                        d2Path,
                        "fmt",
                        tempSourceFile!!.absolutePath
                    ).redirectErrorStream(true).start()

                    val fmtExitCode = fmtProcess.waitFor()
                    if (fmtExitCode == 0) {
                        // Read the formatted content and update the editor
                        val formattedContent = tempSourceFile!!.readText()
                        ApplicationManager.getApplication().invokeLater {
                            isFormatting = true
                            ApplicationManager.getApplication().runWriteAction {
                                editor.document.setText(formattedContent)
                            }
                            isFormatting = false
                        }
                    }
                }

                // Create temp output PNG file directly
                tempOutputFile?.delete()
                tempOutputFile = FileUtil.createTempFile("d2-preview", ".png", true)

                // Execute d2 CLI to generate PNG directly from source
                val settings = D2SettingsState.getInstance(project)
                val d2Path = settings.getEffectiveD2Path()
                val d2Arguments = settings.d2Arguments

                // Build command with arguments
                val command = mutableListOf(d2Path)
                if (d2Arguments.isNotBlank()) {
                    // Split arguments by spaces, preserving quoted strings
                    command.addAll(d2Arguments.split(Regex("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")))
                }
                command.add(tempSourceFile!!.absolutePath)
                command.add(tempOutputFile!!.absolutePath)

                val process = ProcessBuilder(command).redirectErrorStream(true).start()

                val exitCode = process.waitFor()
                
                if (exitCode == 0 && tempOutputFile!!.exists()) {
                    // Load and display the PNG
                    val image = ImageIO.read(tempOutputFile)
                    if (image != null) {
                        ApplicationManager.getApplication().invokeLater {
                            // Clear any previous error text
                            imageLabel.text = null
                            
                            originalImage = image
                            
                            if (isFirstRender) {
                                // First render: calculate zoom to fit
                                zoomLevel = calculateFitToScreenZoom(image)
                                isFirstRender = false
                            } else {
                                // Subsequent renders: restore saved zoom
                                zoomLevel = savedZoom ?: 1.0
                            }
                            
                            updateZoom()
                            
                            // Restore scroll position after a brief delay to ensure layout is complete
                            if (savedScrollPosition != null) {
                                SwingUtilities.invokeLater {
                                    scrollPane.viewport.viewPosition = savedScrollPosition
                                }
                            }
                            
                            showStatus("Updated at ${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))}")
                            updateZoomDisplay()
                            refreshButton.isEnabled = true
                        }
                    } else {
                        showError("Failed to load image")
                        ApplicationManager.getApplication().invokeLater {
                            refreshButton.isEnabled = true
                        }
                    }
                } else {
                    val error = process.inputStream.bufferedReader().readText()
                    showError("D2 rendering failed: $error")
                    ApplicationManager.getApplication().invokeLater {
                        refreshButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                LOG.warn("Failed to update D2 preview", e)
                showError("Error: ${e.message}")
                ApplicationManager.getApplication().invokeLater {
                    refreshButton.isEnabled = true
                }
            }
        }.start()
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
            statusLabel.text = "Error"
        }
    }

    private fun showStatus(message: String) {
        ApplicationManager.getApplication().invokeLater {
            statusLabel.text = message
        }
    }
    
    private fun exportToPreview() {
        tempOutputFile?.let { pngFile ->
            if (pngFile.exists()) {
                try {
                    // Create a persistent export file in user's temp directory
                    val exportFile = File(System.getProperty("java.io.tmpdir"), "d2-export-${System.currentTimeMillis()}.png")
                    pngFile.copyTo(exportFile, overwrite = true)
                    
                    // Open with system default application (Preview on macOS)
                    val osName = System.getProperty("os.name").lowercase()
                    val process = when {
                        osName.contains("mac") -> {
                            ProcessBuilder("open", exportFile.absolutePath).start()
                        }
                        osName.contains("win") -> {
                            ProcessBuilder("cmd", "/c", "start", "", exportFile.absolutePath).start()
                        }
                        osName.contains("nix") || osName.contains("nux") -> {
                            ProcessBuilder("xdg-open", exportFile.absolutePath).start()
                        }
                        else -> {
                            showStatus("Export: OS not supported")
                            return
                        }
                    }
                    
                    showStatus("Exported to ${exportFile.name}")
                } catch (e: Exception) {
                    LOG.warn("Failed to export preview", e)
                    showStatus("Export failed: ${e.message}")
                }
            } else {
                showStatus("No preview to export. Click Refresh first.")
            }
        } ?: showStatus("No preview to export. Click Refresh first.")
    }

    override fun dispose() {
        alarm.cancelAllRequests()
        tempOutputFile?.delete()
        tempSourceFile?.delete()
    }
}
