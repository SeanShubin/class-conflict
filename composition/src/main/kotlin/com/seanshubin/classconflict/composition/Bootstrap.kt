package com.seanshubin.classconflict.composition

class Bootstrap(
    private val integrations: Integrations
) {
    fun loadConfigBaseName(): String {
        val args = integrations.commandLineArguments()
        return if (args.isEmpty()) "class-conflict" else args[0]
    }

    fun createDependencies(): ApplicationDependencies =
        ApplicationDependencies(integrations, loadConfigBaseName())
}
