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
        lines.add("Conflict groups found: ${report.conflictGroups.size}")
        lines.add("Conflicting versions found: ${report.conflictingVersionsFound}")
        lines.add("")

        if (report.hasConflicts) {
            lines.add("Conflict Groups:")
            lines.add("-".repeat(80))

            for ((index, group) in report.conflictGroups.withIndex()) {
                lines.add("")
                lines.add("Group ${index + 1}: ${group.conflicts.size} classes in ${group.artifacts.size} archives")
                lines.add("  Archives:")
                for ((i, artifact) in group.artifacts.withIndex()) {
                    lines.add("    ${i + 1}. $artifact")
                }
                lines.add("  Classes:")
                for (conflict in group.conflicts) {
                    lines.add("    ${conflict.fullyQualifiedName}")
                    for (instance in conflict.instances) {
                        lines.add("      ${instance.artifact}  [${instance.hash}]")
                    }
                }
            }
        } else {
            lines.add("No conflicts detected!")
        }

        return lines
    }
}
