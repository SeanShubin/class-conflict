package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.composition.Integrations
import com.seanshubin.classconflict.di.contract.FilesContract
import com.seanshubin.classconflict.di.delegate.FilesDelegate

class TestIntegrations(
    private val args: Array<String> = emptyArray()
) : Integrations {
    private val outputLines = mutableListOf<String>()

    override fun commandLineArguments(): Array<String> = args
    override fun emitLine(line: String) { outputLines.add(line) }
    override val files: FilesContract = FilesDelegate.defaultInstance()
    override val clock: () -> Long = System::currentTimeMillis

    fun getOutput(): List<String> = outputLines.toList()
    fun getOutputAsText(): String = outputLines.joinToString("\n")
}
