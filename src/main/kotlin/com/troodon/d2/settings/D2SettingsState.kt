package com.troodon.d2.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

const val DEFAULT_D2_ARGUMENTS = "--animate-interval=1000"
const val DEFAULT_DEBOUNCE_DELAY = 1000
const val DEFAULT_PREVIEW_BACKGROUND = "IDE Theme"
const val DEFAULT_PREVIEW_BACKGROUND_CUSTOM_COLOR = "#ffffff"

@Service(Service.Level.PROJECT)
@State(name = "D2Settings", storages = [Storage("d2Settings.xml")])
class D2SettingsState : PersistentStateComponent<D2SettingsState> {

    var d2CliPath: String = "" // Empty by default - will auto-detect from common paths
    var d2Arguments: String = DEFAULT_D2_ARGUMENTS // Additional arguments to pass to d2 command (e.g., --sketch)
    var debounceDelay: Int = DEFAULT_DEBOUNCE_DELAY // Auto-refresh delay in milliseconds
    var previewBackground: String = DEFAULT_PREVIEW_BACKGROUND // Preview background mode
    var previewBackgroundCustomColor: String = DEFAULT_PREVIEW_BACKGROUND_CUSTOM_COLOR // Custom background color hex
    var useWsl: Boolean = false // Whether to run D2 CLI through WSL2
    var wslDistribution: String = "" // WSL distribution name (empty = default)

    /**
     * Gets the effective D2 CLI path to use for execution.
     * If d2CliPath is empty, attempts to find D2 in common locations.
     * Returns the configured path, or a found path, or "d2" as fallback.
     */
    fun getEffectiveD2Path(): String {
        if (d2CliPath.isNotBlank()) {
            return d2CliPath
        }

        // Try to find D2 in common locations
        val validation = D2CliValidator.validateInstallation("", useWsl, wslDistribution)
        if (validation.isInstalled && validation.foundPath != null) {
            return validation.foundPath
        }

        // Fallback to "d2" (will check PATH)
        return "d2"
    }

    override fun getState(): D2SettingsState = this

    override fun loadState(state: D2SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): D2SettingsState {
            return project.getService(D2SettingsState::class.java)
        }

        val SETTINGS_CHANGED_TOPIC: Topic<SettingsChangeListener> = Topic.create(
            "D2 Settings Changed",
            SettingsChangeListener::class.java
        )
    }

    interface SettingsChangeListener {
        fun settingsChanged()
    }
}
