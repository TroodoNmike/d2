package com.troodon.d2.editor

import com.intellij.lang.Commenter

class D2Commenter : Commenter {
    override fun getLineCommentPrefix(): String = "# "

    override fun getBlockCommentPrefix(): String = "\"\"\""

    override fun getBlockCommentSuffix(): String = "\"\"\""

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}
