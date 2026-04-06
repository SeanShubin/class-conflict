package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.composition.ApplicationDependencies
import com.seanshubin.classconflict.composition.Integrations
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val integrations = ProductionIntegrations(args.toList())
    val dependencies = ApplicationDependencies(integrations)
    val exitCode = dependencies.application.run()
    exitProcess(exitCode)
}

class ProductionIntegrations(
    private val args: List<String>
) : Integrations {
    override fun commandLineArguments(): List<String> = args

    override fun emitLine(line: String) {
        println(line)
    }
}
