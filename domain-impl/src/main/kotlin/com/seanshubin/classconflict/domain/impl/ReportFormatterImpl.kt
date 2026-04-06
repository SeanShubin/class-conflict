package com.seanshubin.classconflict.domain.impl

import com.seanshubin.classconflict.domain.api.ClassConflictReport
import com.seanshubin.classconflict.domain.api.ReportFormatter

class ReportFormatterImpl : ReportFormatter {
    override fun format(report: ClassConflictReport): List<String> {
        val lines = mutableListOf<String>()

        lines.add("Class Conflict Report")
        lines.add("=".repeat(80))
        lines.add("")
        lines.add("Classes scanned: ${report.classesScanned}")
        lines.add("Conflicts found: ${report.conflicts.size}")
        lines.add("")

        if (report.hasConflicts) {
            lines.add("Conflicts:")
            lines.add("-".repeat(80))

            for (conflict in report.conflicts) {
                lines.add("")
                lines.add("Class: ${conflict.fullyQualifiedName}")
                lines.add("  Found in ${conflict.instances.size} different versions:")

                for ((index, instance) in conflict.instances.withIndex()) {
                    lines.add("    ${index + 1}. ${instance.artifact.fileName}")
                    lines.add("       Hash: ${instance.hash}")
                }
            }
        } else {
            lines.add("No conflicts detected!")
        }

        return lines
    }
}
