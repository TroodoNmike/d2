package com.troodon.d2.preview

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
import com.intellij.util.ui.JBUI
import com.troodon.d2.settings.D2SettingsConfigurable
import com.troodon.d2.settings.D2SettingsState
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.io.File
import javax.swing.*

class D2PreviewPanel(
    private val project: Project,
    private val file: VirtualFile,
    private val editor: Editor
) : Disposable {

    private val LOG = Logger.getInstance(D2PreviewPanel::class.java)
    private val panel = JPanel(BorderLayout())
    private val statusLabel = JLabel("<html> </html>")
    private val refreshButton = JButton("Refresh", AllIcons.Actions.Refresh)
    private val zoomInButton = JButton("Zoom In", AllIcons.General.ZoomIn)
    private val zoomOutButton = JButton("Zoom Out", AllIcons.General.ZoomOut)
    private val moreActionsButton = JButton(AllIcons.Actions.More).apply {
        // Make it icon-only and compact
        text = null
        toolTipText = "More actions"
        preferredSize = java.awt.Dimension(24, 24)
        minimumSize = java.awt.Dimension(24, 24)
        maximumSize = java.awt.Dimension(24, 24)
        isBorderPainted = false
        isFocusPainted = false
        isContentAreaFilled = false
        cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
    }
    private val autoRefreshCheckBox = JCheckBox("Auto-refresh", true)
    private val autoFormatCheckBox = JCheckBox("Auto-format (d2 fmt)", false)
    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val debounceDelay = 1000 // milliseconds

    // Preview renderers
    private val svgRenderer = SvgPreviewRenderer(project)
    private val gifRenderer = GifPreviewRenderer(project)
    private var currentRenderer: PreviewRenderer = svgRenderer

    // Radio buttons for renderer selection
    private val svgRadioButton = JRadioButton("SVG", true)
    private val pngRadioButton = JRadioButton("PNG", false)

    private var tempOutputFile: File? = null
    private var tempSourceFile: File? = null
    private var isFormatting = false

    private val contentPanel = JPanel(BorderLayout())

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
        project.messageBus.connect(this).subscribe(
            FileDocumentManagerListener.TOPIC,
            fileSaveListener
        )
        refreshButton.addActionListener {
            updatePreview()
        }
        
        zoomInButton.addActionListener {
            currentRenderer.zoomIn()
        }

        zoomOutButton.addActionListener {
            currentRenderer.zoomOut()
        }

        moreActionsButton.addActionListener { event ->
            val popupMenu = JBPopupMenu()

            val exportMenuItem = JMenuItem("Export", AllIcons.ToolbarDecorator.Export)
            exportMenuItem.addActionListener {
                exportToPreview()
            }
            popupMenu.add(exportMenuItem)

            val settingsMenuItem = JMenuItem("Settings", AllIcons.General.Settings)
            settingsMenuItem.addActionListener {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, D2SettingsConfigurable::class.java)
            }
            popupMenu.add(settingsMenuItem)

            popupMenu.show(moreActionsButton, 0, moreActionsButton.height)
        }

        // Setup radio buttons
        val buttonGroup = ButtonGroup()
        buttonGroup.add(svgRadioButton)
        buttonGroup.add(pngRadioButton)

        svgRadioButton.addActionListener {
            switchRenderer(svgRenderer)
        }

        pngRadioButton.addActionListener {
            switchRenderer(gifRenderer)
        }

        // Setup top toolbar with zoom and export buttons
        val topToolbar = JPanel(BorderLayout())
        topToolbar.border = JBUI.Borders.empty(2, 5)

        val tipLabel = JLabel("<html>Tip: Click and drag to pan, Ctrl+scroll to zoom</html>")
        tipLabel.font = tipLabel.font.deriveFont(11f)

        val tipPanel = JPanel(BorderLayout())
        tipPanel.add(tipLabel, BorderLayout.CENTER)

        val topButtonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
        topButtonPanel.add(pngRadioButton)
        topButtonPanel.add(svgRadioButton)
        topButtonPanel.add(zoomOutButton)
        topButtonPanel.add(zoomInButton)
        topButtonPanel.add(moreActionsButton)

        topToolbar.add(tipPanel, BorderLayout.CENTER)
        topToolbar.add(topButtonPanel, BorderLayout.EAST)

        // Setup status bar at the bottom with refresh controls
        val statusPanel = JPanel(BorderLayout())
        statusPanel.border = JBUI.Borders.empty(2, 5)
        statusLabel.font = statusLabel.font.deriveFont(11f)
        statusLabel.verticalAlignment = JLabel.TOP

        // Wrap statusLabel in a panel to constrain width
        val statusLabelPanel = JPanel(BorderLayout())
        statusLabelPanel.add(statusLabel, BorderLayout.CENTER)

        val bottomButtonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
        bottomButtonPanel.add(autoFormatCheckBox)
        bottomButtonPanel.add(autoRefreshCheckBox)
        bottomButtonPanel.add(refreshButton)

        statusPanel.add(statusLabelPanel, BorderLayout.CENTER)
        statusPanel.add(bottomButtonPanel, BorderLayout.EAST)

        panel.add(topToolbar, BorderLayout.NORTH)

        // Initialize with current renderer
        contentPanel.add(currentRenderer.getComponent(), BorderLayout.CENTER)
        panel.add(contentPanel, BorderLayout.CENTER)

        panel.add(statusPanel, BorderLayout.SOUTH)
        panel.border = JBUI.Borders.empty(10)

        updatePreview() // Initial render
    }
    
    private fun switchRenderer(newRenderer: PreviewRenderer) {
        if (currentRenderer != newRenderer) {
            contentPanel.removeAll()
            currentRenderer = newRenderer
            contentPanel.add(currentRenderer.getComponent(), BorderLayout.CENTER)
            contentPanel.revalidate()
            contentPanel.repaint()
            updatePreview()
        }
    }

    private fun updatePreview() {
        refreshButton.isEnabled = false
        showStatus("Rendering...")

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

                // Create temp output file based on current renderer
                tempOutputFile?.delete()
                val extension = currentRenderer.getFileExtension()
                tempOutputFile = FileUtil.createTempFile("d2-preview", extension, true)

                // Execute d2 CLI to generate output file
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
                    // Use the current renderer to display the output
                    currentRenderer.render(tempSourceFile!!, tempOutputFile!!)

                    ApplicationManager.getApplication().invokeLater {
                        showStatus("Updated at ${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))}")
                        refreshButton.isEnabled = true
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

    private fun showError(message: String) {
        ApplicationManager.getApplication().invokeLater {
            statusLabel.text = "<html>Error: $message</html>"
            LOG.warn("Preview error: $message")
        }
    }

    private fun showStatus(message: String) {
        ApplicationManager.getApplication().invokeLater {
            statusLabel.text = "<html>$message</html>"
        }
    }
    
    private fun exportToPreview() {
        tempOutputFile?.let { outputFile ->
            if (outputFile.exists()) {
                try {
                    val extension = currentRenderer.getFileExtension()
                    // Create a persistent export file in user's temp directory
                    val exportFile = File(System.getProperty("java.io.tmpdir"), "d2-export-${System.currentTimeMillis()}$extension")
                    outputFile.copyTo(exportFile, overwrite = true)

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
        svgRenderer.dispose()
        gifRenderer.dispose()
        tempOutputFile?.delete()
        tempSourceFile?.delete()
    }
}
