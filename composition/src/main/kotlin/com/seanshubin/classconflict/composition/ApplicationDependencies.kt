package com.seanshubin.classconflict.composition

import com.seanshubin.classconflict.domain.api.ClassConflictDetector
import com.seanshubin.classconflict.domain.api.ClassScanner
import com.seanshubin.classconflict.domain.api.ReportFormatter
import com.seanshubin.classconflict.domain.api.ReportWriter
import com.seanshubin.classconflict.domain.impl.ClassConflictDetectorImpl
import com.seanshubin.classconflict.domain.impl.ClassScannerImpl
import com.seanshubin.classconflict.domain.impl.ReportFormatterImpl
import com.seanshubin.classconflict.domain.impl.ReportWriterImpl

class ApplicationDependencies(
    private val integrations: Integrations
) {
    val classScanner: ClassScanner = ClassScannerImpl()
    val classConflictDetector: ClassConflictDetector = ClassConflictDetectorImpl(classScanner)
    val reportFormatter: ReportFormatter = ReportFormatterImpl()
    val reportWriter: ReportWriter = ReportWriterImpl()
    val application: Application = Application(
        integrations = integrations,
        classConflictDetector = classConflictDetector,
        reportFormatter = reportFormatter,
        reportWriter = reportWriter
    )
}
