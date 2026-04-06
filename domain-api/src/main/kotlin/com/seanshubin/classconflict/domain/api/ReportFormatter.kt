package com.seanshubin.classconflict.domain.api

interface ReportFormatter {
    fun format(report: ClassConflictReport): List<String>
}
