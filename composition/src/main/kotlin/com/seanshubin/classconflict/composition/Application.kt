package com.seanshubin.classconflict.composition

import com.seanshubin.classconflict.domain.api.ClassConflictDetector
import com.seanshubin.classconflict.domain.api.ReportFormatter
import com.seanshubin.classconflict.domain.api.ReportWriter
import java.nio.file.Paths

class Application(
    private val integrations: Integrations,
    private val classConflictDetector: ClassConflictDetector,
    private val reportFormatter: ReportFormatter,
    private val reportWriter: ReportWriter
) {
    fun run(): Int {
        val args = integrations.commandLineArguments()

        if (args.isEmpty()) {
            integrations.emitLine("Usage: class-conflict [--output-dir <dir>] <artifact1> [artifact2] [artifact3] ...")
            integrations.emitLine("Where artifacts are .jar or .zip files to scan for class conflicts")
            integrations.emitLine("")
            integrations.emitLine("Options:")
            integrations.emitLine("  --output-dir <dir>  Directory for detailed reports (default: generated/class-conflict)")
            return 1
        }

        val (outputDir, artifacts) = parseArguments(args)
        val report = classConflictDetector.detectConflicts(artifacts)

        reportWriter.writeReports(report, outputDir)

        val lines = reportFormatter.format(report)
        lines.forEach { line ->
            integrations.emitLine(line)
        }

        integrations.emitLine("")
        integrations.emitLine("Detailed reports written to:")
        integrations.emitLine("  Count:  ${outputDir.resolve("count/quality-metrics.json")}")
        integrations.emitLine("  Diff:   ${outputDir.resolve("diff/")}")
        integrations.emitLine("  Browse: ${outputDir.resolve("browse/")}")

        return if (report.hasConflicts) 1 else 0
    }

    private fun parseArguments(args: List<String>): Pair<java.nio.file.Path, List<java.nio.file.Path>> {
        var outputDir = Paths.get("generated/class-conflict")
        val artifactArgs = mutableListOf<String>()

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "--output-dir" -> {
                    if (i + 1 < args.size) {
                        outputDir = Paths.get(args[i + 1])
                        i += 2
                    } else {
                        i++
                    }
                }
                else -> {
                    artifactArgs.add(args[i])
                    i++
                }
            }
        }

        val artifacts = artifactArgs.map { Paths.get(it) }
        return Pair(outputDir, artifacts)
    }
}
