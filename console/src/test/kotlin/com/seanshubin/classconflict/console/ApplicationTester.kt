package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.composition.Bootstrap
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class ApplicationTester {
    private val tempDir: Path = Files.createTempDirectory("class-conflict-test")
    private val configBaseName: String = tempDir.resolve("test-config").toString()
    private val testIntegrations = TestIntegrations()

    fun createJarWithClass(jarName: String, className: String, classContent: ByteArray): Path {
        val jarPath = tempDir.resolve(jarName)
        val classFileName = className.replace('.', '/') + ".class"

        JarOutputStream(Files.newOutputStream(jarPath)).use { jos ->
            jos.putNextEntry(JarEntry(classFileName))
            jos.write(classContent)
            jos.closeEntry()
        }

        return jarPath
    }

    fun runApplication(): Int {
        writeConfig()
        val integrationsWithArgs = TestIntegrations(arrayOf(configBaseName))
        val dependencies = Bootstrap(integrationsWithArgs).createDependencies()
        val exitCode = dependencies.application.run()
        integrationsWithArgs.getOutput().forEach { testIntegrations.emitLine(it) }
        return exitCode
    }

    fun runApplicationWithNoArgs(): Int {
        writeConfig()
        val integrationsWithArgs = TestIntegrations(emptyArray())
        val dependencies = Bootstrap(integrationsWithArgs).createDependencies()
        val exitCode = dependencies.application.run()
        integrationsWithArgs.getOutput().forEach { testIntegrations.emitLine(it) }
        return exitCode
    }

    fun outputContains(text: String): Boolean {
        return testIntegrations.getOutputAsText().contains(text)
    }

    fun cleanup() {
        deleteRecursively(tempDir)
    }

    private fun writeConfig() {
        val configFile = Files.writeString(
            tempDir.resolve("test-config-config.json"),
            """{
  "inputDir" : "${tempDir.toString().replace("\\", "\\\\")}",
  "outputDir" : "${tempDir.resolve("output").toString().replace("\\", "\\\\")}"
}"""
        )
    }

    private fun deleteRecursively(path: Path) {
        if (Files.isDirectory(path)) {
            Files.list(path).use { stream -> stream.forEach { deleteRecursively(it) } }
        }
        Files.deleteIfExists(path)
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
