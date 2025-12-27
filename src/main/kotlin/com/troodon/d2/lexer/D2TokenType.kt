package com.troodon.d2.lexer

import com.intellij.psi.tree.IElementType
import com.troodon.d2.lang.D2Language

class D2TokenType(debugName: String) :
    IElementType(debugName, D2Language.INSTANCE)