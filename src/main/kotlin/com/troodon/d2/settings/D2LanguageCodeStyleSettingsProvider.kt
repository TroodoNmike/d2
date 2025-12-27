package com.troodon.d2.settings

import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.troodon.d2.lang.D2Language

class D2LanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage() = D2Language.INSTANCE

    override fun getCodeSample(settingsType: SettingsType): String {
        return """
            x -> y: {
              style.animated: true
            }
        """.trimIndent()
    }

    override fun customizeDefaults(
        commonSettings: com.intellij.psi.codeStyle.CommonCodeStyleSettings,
        indentOptions: com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions
    ) {
        indentOptions.INDENT_SIZE = 2
        indentOptions.TAB_SIZE = 2
        indentOptions.CONTINUATION_INDENT_SIZE = 2
    }
}
