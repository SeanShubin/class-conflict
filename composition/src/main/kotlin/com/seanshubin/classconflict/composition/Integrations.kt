package com.seanshubin.classconflict.composition

interface Integrations {
    fun commandLineArguments(): List<String>
    fun emitLine(line: String)
}
