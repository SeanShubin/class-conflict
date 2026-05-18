package com.seanshubin.classconflict.composition

import com.seanshubin.classconflict.di.contract.FilesContract

interface Integrations {
    fun commandLineArguments(): List<String>
    fun emitLine(line: String)
    val files: FilesContract
    val clock: () -> Long
}
