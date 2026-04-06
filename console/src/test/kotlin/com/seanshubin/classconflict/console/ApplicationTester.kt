package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.composition.ApplicationDependencies
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class ApplicationTester {
    private val tempDir: Path = Files.createTempDirectory("class-conflict-test")
    private val testIntegrations = TestIntegrations()
    private val createdArtifacts = mutableListOf<Path>()

    fun createJarWithClass(jarName: String, className: String, classContent: ByteArray): Path {
        val jarPath = tempDir.resolve(jarName)
        val classFileName = className.replace('.', '/') + ".class"

        JarOutputStream(Files.newOutputStream(jarPath)).use { jos ->
            jos.putNextEntry(JarEntry(classFileName))
            jos.write(classContent)
            jos.closeEntry()
        }

        createdArtifacts.add(jarPath)
        return jarPath
    }

    fun runApplication(artifacts: List<Path>): Int {
        val args = artifacts.map { it.toString() }
        val integrationsWithArgs = TestIntegrations(args)
        val dependencies = ApplicationDependencies(integrationsWithArgs)
        val exitCode = dependencies.application.run()

        integrationsWithArgs.getOutput().forEach { testIntegrations.emitLine(it) }

        return exitCode
    }

    fun getOutput(): List<String> = testIntegrations.getOutput()

    fun outputContains(text: String): Boolean {
        return testIntegrations.getOutputAsText().contains(text)
    }

    fun cleanup() {
        createdArtifacts.forEach { Files.deleteIfExists(it) }
        Files.deleteIfExists(tempDir)
    }

    companion object {
        fun createSimpleClassFile(content: String): ByteArray {
            val out = ByteArrayOutputStream()

            out.write(0xCA.toByte().toInt())
            out.write(0xFE.toByte().toInt())
            out.write(0xBA.toByte().toInt())
            out.write(0xBE.toByte().toInt())

            out.write(0)
            out.write(0)

            out.write(0)
            out.write(52)

            out.write(content.toByteArray())

            return out.toByteArray()
        }
    }
}
