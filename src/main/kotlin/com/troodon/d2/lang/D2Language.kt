package com.troodon.d2.lang

import com.intellij.lang.Language

class D2Language private constructor() : Language("D2") {
    companion object {
        val INSTANCE = D2Language()
    }
}
