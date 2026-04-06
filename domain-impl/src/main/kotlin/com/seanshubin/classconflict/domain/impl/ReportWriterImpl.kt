package com.seanshubin.classconflict.domain.impl

import com.seanshubin.classconflict.domain.api.ClassConflict
import com.seanshubin.classconflict.domain.api.ClassConflictReport
import com.seanshubin.classconflict.domain.api.ReportWriter
import java.nio.file.Files
import java.nio.file.Path

class ReportWriterImpl : ReportWriter {
    override fun writeReports(report: ClassConflictReport, outputDir: Path) {
        writeCountReport(report, outputDir)
        writeDiffReports(report, outputDir)
        writeBrowseReports(report, outputDir)
    }

    private fun writeCountReport(report: ClassConflictReport, outputDir: Path) {
        val countDir = outputDir.resolve("count")
        Files.createDirectories(countDir)

        val json = buildString {
            appendLine("{")
            appendLine("  \"classesScanned\" : ${report.classesScanned},")
            appendLine("  \"conflictsFound\" : ${report.conflicts.size}")
            appendLine("}")
        }

        val countFile = countDir.resolve("quality-metrics.json")
        Files.writeString(countFile, json)
    }

    private fun writeDiffReports(report: ClassConflictReport, outputDir: Path) {
        val diffDir = outputDir.resolve("diff")
        Files.createDirectories(diffDir)

        for (conflict in report.conflicts) {
            val json = formatConflictAsJson(conflict)
            val fileName = "conflict-${conflict.fullyQualifiedName}.json"
            val diffFile = diffDir.resolve(fileName)
            Files.writeString(diffFile, json)
        }
    }

    private fun writeBrowseReports(report: ClassConflictReport, outputDir: Path) {
        val browseDir = outputDir.resolve("browse")
        Files.createDirectories(browseDir)

        writeSummaryReport(report, browseDir)
        writeConflictReports(report, browseDir)
    }

    private fun writeSummaryReport(report: ClassConflictReport, browseDir: Path) {
        val lines = mutableListOf<String>()

        lines.add("Class Conflict Summary")
        lines.add("=".repeat(80))
        lines.add("")
        lines.add("Classes scanned: ${report.classesScanned}")
        lines.add("Conflicts found: ${report.conflicts.size}")
        lines.add("")

        if (report.hasConflicts) {
            lines.add("Conflicts by Package:")
            lines.add("-".repeat(80))

            val conflictsByPackage = report.conflicts.groupBy { extractPackage(it.fullyQualifiedName) }
            for ((packageName, conflicts) in conflictsByPackage.toSortedMap()) {
                lines.add("")
                lines.add("Package: $packageName")
                lines.add("  ${conflicts.size} conflict(s)")
                for (conflict in conflicts) {
                    val className = extractClassName(conflict.fullyQualifiedName)
                    lines.add("    - $className (${conflict.instances.size} versions)")
                }
            }
        } else {
            lines.add("No conflicts detected!")
        }

        val summaryFile = browseDir.resolve("summary.txt")
        Files.write(summaryFile, lines)
    }

    private fun writeConflictReports(report: ClassConflictReport, browseDir: Path) {
        for (conflict in report.conflicts) {
            val lines = formatConflictAsText(conflict)
            val fileName = "conflict-${conflict.fullyQualifiedName}.txt"
            val conflictFile = browseDir.resolve(fileName)
            Files.write(conflictFile, lines)
        }
    }

    private fun formatConflictAsJson(conflict: ClassConflict): String {
        return buildString {
            appendLine("{")
            appendLine("  \"fullyQualifiedName\" : \"${conflict.fullyQualifiedName}\",")
            appendLine("  \"instances\" : [")
            conflict.instances.forEachIndexed { index, instance ->
                appendLine("    {")
                appendLine("      \"artifact\" : \"${instance.artifact.fileName}\",")
                appendLine("      \"hash\" : \"${instance.hash}\"")
                append("    }")
                if (index < conflict.instances.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ]")
            appendLine("}")
        }
    }

    private fun formatConflictAsText(conflict: ClassConflict): List<String> {
        val lines = mutableListOf<String>()

        lines.add("Class Conflict: ${conflict.fullyQualifiedName}")
        lines.add("=".repeat(80))
        lines.add("")
        lines.add("This class appears in ${conflict.instances.size} different versions:")
        lines.add("")

        for ((index, instance) in conflict.instances.withIndex()) {
            lines.add("Version ${index + 1}:")
            lines.add("  Artifact: ${instance.artifact.fileName}")
            lines.add("  Full path: ${instance.artifact}")
            lines.add("  SHA-256: ${instance.hash}")
            lines.add("")
        }

        lines.add("Impact:")
        lines.add("  The JVM will load whichever version it encounters first, leading to")
        lines.add("  unpredictable behavior. This can cause:")
        lines.add("  - Method not found exceptions at runtime")
        lines.add("  - Unexpected behavior due to different implementations")
        lines.add("  - ClassCastException errors")
        lines.add("")

        lines.add("Resolution:")
        lines.add("  1. Determine which version is correct for your application")
        lines.add("  2. Use dependency management to exclude the unwanted versions")
        lines.add("  3. Consider using a dependency convergence strategy")

        return lines
    }

    private fun extractPackage(fullyQualifiedName: String): String {
        val lastDot = fullyQualifiedName.lastIndexOf('.')
        return if (lastDot > 0) fullyQualifiedName.substring(0, lastDot) else "(default)"
    }

    private fun extractClassName(fullyQualifiedName: String): String {
        val lastDot = fullyQualifiedName.lastIndexOf('.')
        return if (lastDot > 0) fullyQualifiedName.substring(lastDot + 1) else fullyQualifiedName
    }
}
