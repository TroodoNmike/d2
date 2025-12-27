package com.troodon.d2.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class D2SettingsConfigurable(private val project: Project) : Configurable {

    private var settingsPanel: D2SettingsPanel? = null

    override fun getDisplayName(): String = "D2 Diagram"

    override fun createComponent(): JComponent {
        settingsPanel = D2SettingsPanel(project)
        return settingsPanel!!.createPanel()
    }

    override fun isModified(): Boolean = settingsPanel?.isModified() ?: false

    override fun apply() {
        settingsPanel?.apply()
    }

    override fun reset() {
        settingsPanel?.reset()
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
}
