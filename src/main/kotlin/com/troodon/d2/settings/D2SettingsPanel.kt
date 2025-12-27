package com.troodon.d2.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class D2SettingsPanel(private val project: Project) {

    private val d2PathField = TextFieldWithBrowseButton()
    private val versionLabel = JBLabel()
    private val statusLabel = JBLabel()
    private val refreshButton = JButton("Validate")

    fun createPanel(): JComponent {
        // Setup D2 CLI path field with file browser
        val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
            .withTitle("Select D2 CLI Executable")
            .withDescription("Choose the d2 executable file")
        d2PathField.addActionListener {
            com.intellij.openapi.fileChooser.FileChooser.chooseFile(descriptor, project, null) { file ->
                d2PathField.text = file.path
            }
        }

        refreshButton.addActionListener {
            updateVersion()
        }

        // Load current setting
        val settings = D2SettingsState.getInstance(project)
        d2PathField.text = settings.d2CliPath

        updateVersion()

        val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        statusPanel.add(statusLabel)
        statusPanel.add(refreshButton)

        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("D2 CLI Path:", d2PathField)
            .addLabeledComponent("D2 CLI Status:", statusPanel)
            .addLabeledComponent("D2 Version:", versionLabel)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())
        mainPanel.add(panel, BorderLayout.NORTH)
        mainPanel.border = JBUI.Borders.empty(10)

        return mainPanel
    }

    fun getD2CliPath(): String = d2PathField.text

    fun setD2CliPath(path: String) {
        d2PathField.text = path
    }

    fun isModified(): Boolean {
        val settings = D2SettingsState.getInstance(project)
        return d2PathField.text != settings.d2CliPath
    }

    fun apply() {
        val settings = D2SettingsState.getInstance(project)
        settings.d2CliPath = d2PathField.text
        updateVersion()
    }

    fun reset() {
        val settings = D2SettingsState.getInstance(project)
        d2PathField.text = settings.d2CliPath
        updateVersion()
    }

    fun updateVersion() {
        statusLabel.text = "Checking..."
        statusLabel.foreground = UIUtil.getLabelForeground()
        refreshButton.isEnabled = false

        // Run validation in background to avoid blocking UI
        Thread {
            val d2Path = d2PathField.text
            val validation = D2CliValidator.validateInstallation(d2Path)

            javax.swing.SwingUtilities.invokeLater {
                if (validation.isInstalled) {
                    statusLabel.text = "✓ Installed"
                    statusLabel.foreground = UIUtil.getLabelSuccessForeground()

                    // Display version and full path
                    val versionText = validation.version ?: "Unknown"
                    val pathInfo = if (validation.foundPath != null) {
                        " (${validation.foundPath})"
                    } else {
                        ""
                    }
                    versionLabel.text = "$versionText$pathInfo"
                    versionLabel.foreground = UIUtil.getLabelForeground()

                    // Auto-update path if found in common location and current path is empty
                    if (validation.foundPath != null && d2PathField.text.isBlank()) {
                        d2PathField.text = validation.foundPath
                    }
                } else {
                    statusLabel.text = "✗ Not found"
                    statusLabel.foreground = UIUtil.getErrorForeground()

                    versionLabel.text = validation.error ?: "D2 CLI not found"
                    versionLabel.foreground = UIUtil.getErrorForeground()
                    versionLabel.font = versionLabel.font.deriveFont(Font.PLAIN, 11f)
                }
                refreshButton.isEnabled = true
            }
        }.start()
    }
}
