package com.troodon.d2.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JColorChooser
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

class D2SettingsPanel(private val project: Project) {

    private val d2PathField = TextFieldWithBrowseButton()
    private val d2ArgumentsField = JBTextField()
    private val resetArgumentsButton = JButton(AllIcons.Actions.Rollback).apply {
        toolTipText = "Reset to default value"
        preferredSize = java.awt.Dimension(24, 24)
        isBorderPainted = false
        isFocusPainted = false
        isContentAreaFilled = false
    }
    private val debounceDelayField = JBTextField()
    private val resetDebounceButton = JButton(AllIcons.Actions.Rollback).apply {
        toolTipText = "Reset to default value"
        preferredSize = java.awt.Dimension(24, 24)
        isBorderPainted = false
        isFocusPainted = false
        isContentAreaFilled = false
    }
    private val previewBackgroundCombo = JComboBox(arrayOf("IDE Theme", "Transparent", "Light", "Dark", "Custom"))
    private val customColorButton = JButton().apply {
        preferredSize = Dimension(24, 24)
        toolTipText = "Choose custom color"
    }
    private val useWslCheckBox = JCheckBox("Use WSL to run D2")
    private val wslDistributionField = JBTextField().apply {
        toolTipText = "Leave empty for default distribution"
    }
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

        resetArgumentsButton.addActionListener {
            d2ArgumentsField.text = DEFAULT_D2_ARGUMENTS
        }

        resetDebounceButton.addActionListener {
            debounceDelayField.text = DEFAULT_DEBOUNCE_DELAY.toString()
        }

        previewBackgroundCombo.addActionListener {
            customColorButton.isVisible = previewBackgroundCombo.selectedItem == "Custom"
        }

        customColorButton.addActionListener {
            val current = try { Color.decode(customColorButton.name ?: DEFAULT_PREVIEW_BACKGROUND_CUSTOM_COLOR) } catch (_: Exception) { Color.WHITE }
            val chosen = JColorChooser.showDialog(customColorButton, "Choose Preview Background Color", current)
            if (chosen != null) {
                customColorButton.name = String.format("#%02x%02x%02x", chosen.red, chosen.green, chosen.blue)
                customColorButton.background = chosen
            }
        }

        useWslCheckBox.addActionListener {
            wslDistributionField.isEnabled = useWslCheckBox.isSelected
        }

        // Load current setting
        val settings = D2SettingsState.getInstance(project)
        d2PathField.text = settings.d2CliPath
        d2ArgumentsField.text = settings.d2Arguments
        debounceDelayField.text = settings.debounceDelay.toString()
        previewBackgroundCombo.selectedItem = settings.previewBackground
        customColorButton.name = settings.previewBackgroundCustomColor
        try { customColorButton.background = Color.decode(settings.previewBackgroundCustomColor) } catch (_: Exception) {}
        customColorButton.isVisible = settings.previewBackground == "Custom"
        useWslCheckBox.isSelected = settings.useWsl
        wslDistributionField.text = settings.wslDistribution
        wslDistributionField.isEnabled = settings.useWsl

        updateVersion()

        val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        statusPanel.add(statusLabel)
        statusPanel.add(refreshButton)

        // Create a panel for d2Arguments field with reset button
        val argumentsPanel = JPanel(BorderLayout(5, 0))
        argumentsPanel.add(d2ArgumentsField, BorderLayout.CENTER)
        argumentsPanel.add(resetArgumentsButton, BorderLayout.EAST)

        // Create a panel for debounceDelay field with reset button
        val debouncePanel = JPanel(BorderLayout(5, 0))
        debouncePanel.add(debounceDelayField, BorderLayout.CENTER)
        debouncePanel.add(resetDebounceButton, BorderLayout.EAST)

        // Create a panel for preview background combo with color button
        val previewBgPanel = JPanel(BorderLayout(5, 0))
        previewBgPanel.add(previewBackgroundCombo, BorderLayout.CENTER)
        previewBgPanel.add(customColorButton, BorderLayout.EAST)

        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("D2 CLI Path:", d2PathField)
            .addComponent(useWslCheckBox)
            .addTooltip("Run D2 CLI through WSL2 (Windows Subsystem for Linux)")
            .addLabeledComponent("WSL Distribution:", wslDistributionField)
            .addTooltip("Leave empty to use the default WSL distribution")
            .addLabeledComponent("D2 Arguments:", argumentsPanel)
            .addTooltip("Additional arguments to pass to d2 command (e.g., --sketch, --theme=200 --animate-interval=1000)")
            .addLabeledComponent("Auto-refresh", debouncePanel)
            .addTooltip("Delay in milliseconds before auto-refreshing the preview after typing")
            .addLabeledComponent("Preview Background:", previewBgPanel)
            .addTooltip("Background color for the SVG preview panel")
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
        return d2PathField.text != settings.d2CliPath ||
               d2ArgumentsField.text != settings.d2Arguments ||
               debounceDelayField.text.toIntOrNull() != settings.debounceDelay ||
               previewBackgroundCombo.selectedItem as String != settings.previewBackground ||
               (customColorButton.name ?: DEFAULT_PREVIEW_BACKGROUND_CUSTOM_COLOR) != settings.previewBackgroundCustomColor ||
               useWslCheckBox.isSelected != settings.useWsl ||
               wslDistributionField.text != settings.wslDistribution
    }

    fun apply() {
        val settings = D2SettingsState.getInstance(project)
        settings.d2CliPath = d2PathField.text
        settings.d2Arguments = d2ArgumentsField.text.trim()
        settings.debounceDelay = debounceDelayField.text.toIntOrNull() ?: DEFAULT_DEBOUNCE_DELAY
        settings.previewBackground = previewBackgroundCombo.selectedItem as String
        settings.previewBackgroundCustomColor = customColorButton.name ?: DEFAULT_PREVIEW_BACKGROUND_CUSTOM_COLOR
        settings.useWsl = useWslCheckBox.isSelected
        settings.wslDistribution = wslDistributionField.text.trim()
        updateVersion()
    }

    fun reset() {
        val settings = D2SettingsState.getInstance(project)
        d2PathField.text = settings.d2CliPath
        d2ArgumentsField.text = settings.d2Arguments
        debounceDelayField.text = settings.debounceDelay.toString()
        previewBackgroundCombo.selectedItem = settings.previewBackground
        customColorButton.name = settings.previewBackgroundCustomColor
        try { customColorButton.background = Color.decode(settings.previewBackgroundCustomColor) } catch (_: Exception) {}
        customColorButton.isVisible = settings.previewBackground == "Custom"
        useWslCheckBox.isSelected = settings.useWsl
        wslDistributionField.text = settings.wslDistribution
        wslDistributionField.isEnabled = settings.useWsl
        updateVersion()
    }

    fun updateVersion() {
        statusLabel.text = "Checking..."
        statusLabel.foreground = UIUtil.getLabelForeground()
        refreshButton.isEnabled = false

        // Run validation in background to avoid blocking UI
        Thread {
            val d2Path = d2PathField.text
            val validation = D2CliValidator.validateInstallation(
                d2Path, useWslCheckBox.isSelected, wslDistributionField.text.trim()
            )

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
