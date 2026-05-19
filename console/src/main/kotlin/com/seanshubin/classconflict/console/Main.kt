package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.composition.Bootstrap
import com.seanshubin.classconflict.composition.Integrations
import com.seanshubin.classconflict.di.contract.FilesContract
import com.seanshubin.classconflict.di.delegate.FilesDelegate
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val integrations = ProductionIntegrations(args)
    val dependencies = Bootstrap(integrations).createDependencies()
    val exitCode = dependencies.application.run()
    exitProcess(exitCode)
}

class ProductionIntegrations(
    private val args: Array<String>
) : Integrations {
    override fun commandLineArguments(): Array<String> = args
    override fun emitLine(line: String) = println(line)
    override val files: FilesContract = FilesDelegate.defaultInstance()
    override val clock: () -> Long = System::currentTimeMillis
}
