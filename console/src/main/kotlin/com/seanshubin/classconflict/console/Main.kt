package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.composition.ApplicationDependencies
import com.seanshubin.classconflict.composition.Integrations
import com.seanshubin.classconflict.di.contract.FilesContract
import com.seanshubin.classconflict.di.delegate.FilesDelegate
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
    override fun emitLine(line: String) = println(line)
    override val files: FilesContract = FilesDelegate.defaultInstance()
    override val clock: () -> Long = System::currentTimeMillis
}
