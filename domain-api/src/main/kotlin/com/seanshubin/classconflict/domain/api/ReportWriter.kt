package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

interface ReportWriter {
    fun writeReports(report: ClassConflictReport, outputDir: Path)
}
