package com.troodon.d2.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class D2PsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, D2Language.INSTANCE) {
    override fun getFileType(): FileType = D2FileType()

    override fun toString(): String = "D2 File"
}
