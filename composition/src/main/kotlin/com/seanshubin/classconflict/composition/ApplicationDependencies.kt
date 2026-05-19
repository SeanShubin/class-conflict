package com.seanshubin.classconflict.composition

import com.seanshubin.classconflict.domain.api.ClassConflictDetector
import com.seanshubin.classconflict.domain.api.ClassScanner
import com.seanshubin.classconflict.domain.api.ReportFormatter
import com.seanshubin.classconflict.domain.api.ReportWriter
import com.seanshubin.classconflict.domain.impl.ClassConflictDetectorImpl
import com.seanshubin.classconflict.domain.impl.ClassScannerImpl
import com.seanshubin.classconflict.domain.impl.ReportFormatterImpl
import com.seanshubin.classconflict.domain.impl.ReportWriterImpl
import com.seanshubin.classconflict.fileselection.FileChooser
import com.seanshubin.classconflict.fileselection.FileChooserImpl

class ApplicationDependencies(
    private val integrations: Integrations,
    private val configBaseName: String
) {
    val files = integrations.files
    val configurationLoader = ConfigurationLoader(files, configBaseName)
    val fileChooser: FileChooser = FileChooserImpl(files)
    val classScanner: ClassScanner = ClassScannerImpl()
    val classConflictDetector: ClassConflictDetector = ClassConflictDetectorImpl(classScanner)
    val reportFormatter: ReportFormatter = ReportFormatterImpl()
    val reportWriter: ReportWriter = ReportWriterImpl(files, ApplicationDependencies::class.java.classLoader)
    val application: Application = Application(
        emitLine = integrations::emitLine,
        files = files,
        clock = integrations.clock,
        configurationLoader = configurationLoader,
        fileChooser = fileChooser,
        classConflictDetector = classConflictDetector,
        reportFormatter = reportFormatter,
        reportWriter = reportWriter
    )
}
