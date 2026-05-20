package com.seanshubin.classconflict.domain.impl

import com.seanshubin.classconflict.domain.api.ClassConflictReport
import com.seanshubin.classconflict.domain.api.ErrorType
import com.seanshubin.classconflict.domain.api.ReportFormatter

class ReportFormatterImpl : ReportFormatter {
    override fun format(report: ClassConflictReport): List<String> {
        val summary = report.summary
        val lines = mutableListOf<String>()
        ErrorType.entries.forEach { errorType ->
            val item = summary.errors.getValue(errorType)
            if (item.isPartOfTotal) {
                lines.add("${errorType.caption}: ${item.count} (counted as errors)")
            } else {
                lines.add("${errorType.caption}: ${item.count}")
            }
        }
        lines.add("Total Errors: ${summary.errorCount} of ${summary.errorLimit} errors allowed")
        return lines
    }
}
