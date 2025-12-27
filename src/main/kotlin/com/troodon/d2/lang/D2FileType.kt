package com.troodon.d2.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import com.troodon.d2.icons.D2Icons

class D2FileType : LanguageFileType(D2Language.INSTANCE) {
    override fun getName() = "D2"
    override fun getDescription() = "D2 diagram file"
    override fun getDefaultExtension() = "d2"
    override fun getIcon() = D2Icons.FILE
}
