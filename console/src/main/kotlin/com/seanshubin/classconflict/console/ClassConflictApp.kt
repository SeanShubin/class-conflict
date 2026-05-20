package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.composition.Bootstrap
import kotlin.system.exitProcess

object ClassConflictApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val integrations = ProductionIntegrations(args)
        val dependencies = Bootstrap(integrations).createDependencies()
        val exitCode = dependencies.application.run()
        exitProcess(exitCode)
    }
}
