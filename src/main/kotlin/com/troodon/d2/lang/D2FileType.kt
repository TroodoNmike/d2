package com.troodon.d2.lang

import com.intellij.openapi.fileTypes.LanguageFileType

class D2FileType : LanguageFileType(D2Language.INSTANCE) {
    override fun getName() = "D2"
    override fun getDescription() = "D2 diagram file"
    override fun getDefaultExtension() = "d2"
    override fun getIcon() = null
}
