package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.composition.Integrations

class TestIntegrations(
    private val args: List<String> = emptyList()
) : Integrations {
    private val outputLines = mutableListOf<String>()

    override fun commandLineArguments(): List<String> = args

    override fun emitLine(line: String) {
        outputLines.add(line)
    }

    fun getOutput(): List<String> = outputLines.toList()

    fun getOutputAsText(): String = outputLines.joinToString("\n")
}
