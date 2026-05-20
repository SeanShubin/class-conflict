package com.seanshubin.classconflict.composition

import com.seanshubin.classconflict.di.contract.FilesContract
import com.seanshubin.classconflict.domain.api.ClassConflictDetector
import com.seanshubin.classconflict.domain.api.ReportFormatter
import com.seanshubin.classconflict.domain.api.ReportWriter
import com.seanshubin.classconflict.duration.format.DurationFormat
import com.seanshubin.classconflict.fileselection.FileChooser
import com.seanshubin.classconflict.fileselection.FileSelection
import java.nio.file.Paths

class Application(
    private val emitLine: (String) -> Unit,
    private val files: FilesContract,
    private val clock: () -> Long,
    private val configurationLoader: ConfigurationLoader,
    private val fileChooser: FileChooser,
    private val classConflictDetector: ClassConflictDetector,
    private val reportFormatter: ReportFormatter,
    private val reportWriter: ReportWriter
) {
    fun run(): Int {
        val startTime = clock()
        val config = configurationLoader.load()

        val inputDir = if (config.inputDir.isAbsolute) {
            config.inputDir
        } else {
            Paths.get("").toAbsolutePath().resolve(config.inputDir)
        }

        if (!files.exists(inputDir)) {
            emitLine("Error: Input directory does not exist: $inputDir")
            emitLine("")
            emitLine("Configuration file: ${configurationLoader.configBaseName}-config.json")
            emitLine("Current inputDir setting: ${config.inputDir}")
            return 1
        }

        val discoveredArtifacts = fileChooser.choose(
            FileSelection(
                baseDir = inputDir,
                includePatterns = config.artifactFileRegexPatterns.include,
                excludePatterns = config.artifactFileRegexPatterns.exclude
            )
        )

        if (discoveredArtifacts.isEmpty()) {
            emitLine("No artifacts found in: $inputDir")
            emitLine("")
            emitLine("Configuration file: ${configurationLoader.configBaseName}-config.json")
            emitLine("Include patterns: ${config.artifactFileRegexPatterns.include}")
            emitLine("Exclude patterns: ${config.artifactFileRegexPatterns.exclude}")
            emitLine("")
            emitLine("Edit the configuration file to specify:")
            emitLine("  - inputDir: Directory to scan for artifacts")
            emitLine("  - artifactFileRegexPatterns.include: Patterns to match artifacts")
            emitLine("  - artifactFileRegexPatterns.exclude: Patterns to exclude")
            return 1
        }

        val report = classConflictDetector.detectConflicts(config.copy(inputDir = inputDir), discoveredArtifacts)

        reportWriter.writeReports(report, config.outputDir)

        val lines = reportFormatter.format(report)
        lines.forEach { line ->
            emitLine(line)
        }

        emitLine("Output: ${config.outputDir}")
        emitLine("Time taken: ${DurationFormat.milliseconds.format(clock() - startTime)}")
        return if (report.summary.isOverLimit) 1 else 0
    }
}
