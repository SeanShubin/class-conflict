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
/*
git checkout -- pom.xml project-specification.json scripts/ commands/pom.xml console/pom.xml core/pom.xml di-contract/pom.xml di-delegate/pom.xml di-test/pom.xml dynamic-core/pom.xml dynamic-json/pom.xml generator/pom.xml gradle/pom.xml http/pom.xml maven/pom.xml source/pom.xml xml/pom.xml
 */