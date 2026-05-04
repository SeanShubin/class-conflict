package com.seanshubin.classconflict.composition

import com.seanshubin.classconflict.domain.api.ClassConflictDetector
import com.seanshubin.classconflict.domain.api.Configuration
import com.seanshubin.classconflict.domain.api.ReportFormatter
import com.seanshubin.classconflict.domain.api.ReportWriter
import com.seanshubin.classconflict.domain.impl.ArtifactFilter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

class Application(
    private val integrations: Integrations,
    private val classConflictDetector: ClassConflictDetector,
    private val reportFormatter: ReportFormatter,
    private val reportWriter: ReportWriter
) {
    fun run(): Int {
        val args = integrations.commandLineArguments()
        val configBaseName = if (args.isEmpty()) "class-conflict" else args[0]

        val configurationLoader = ConfigurationLoader(integrations, configBaseName)
        val config = configurationLoader.load()

        val inputDir = if (config.inputDir.isAbsolute) {
            config.inputDir
        } else {
            Paths.get("").toAbsolutePath().resolve(config.inputDir)
        }

        if (!Files.exists(inputDir)) {
            integrations.emitLine("Error: Input directory does not exist: $inputDir")
            integrations.emitLine("")
            integrations.emitLine("Configuration file: $configBaseName-config.json")
            integrations.emitLine("Current inputDir setting: ${config.inputDir}")
            return 1
        }

        val artifactFilter = ArtifactFilter(
            inputDir,
            config.artifactFileRegexPatterns.include,
            config.artifactFileRegexPatterns.exclude
        )

        val discoveredArtifacts = Files.walk(inputDir)
            .filter { Files.isRegularFile(it) }
            .filter { artifactFilter(it) }
            .toList()

        if (discoveredArtifacts.isEmpty()) {
            integrations.emitLine("No artifacts found in: $inputDir")
            integrations.emitLine("")
            integrations.emitLine("Configuration file: $configBaseName-config.json")
            integrations.emitLine("Include patterns: ${config.artifactFileRegexPatterns.include}")
            integrations.emitLine("Exclude patterns: ${config.artifactFileRegexPatterns.exclude}")
            integrations.emitLine("")
            integrations.emitLine("Edit the configuration file to specify:")
            integrations.emitLine("  - inputDir: Directory to scan for artifacts")
            integrations.emitLine("  - artifactFileRegexPatterns.include: Patterns to match artifacts")
            integrations.emitLine("  - artifactFileRegexPatterns.exclude: Patterns to exclude")
            return 1
        }

        val report = classConflictDetector.detectConflicts(config, discoveredArtifacts)

        reportWriter.writeReports(report, config.outputDir)

        val lines = reportFormatter.format(report)
        lines.forEach { line ->
            integrations.emitLine(line)
        }

        integrations.emitLine("")
        integrations.emitLine("Detailed reports written to:")
        integrations.emitLine("  Count:  ${config.outputDir.resolve("count/quality-metrics.json")}")
        integrations.emitLine("  Diff:   ${config.outputDir.resolve("diff/")}")
        integrations.emitLine("  Browse: ${config.outputDir.resolve("browse/")}")

        return if (report.hasConflicts) 1 else 0
    }
}
