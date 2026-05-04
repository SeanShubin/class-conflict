package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

data class ClassConflictReport(
    val configuration: Configuration,
    val artifacts: List<Path>,
    val allClasses: List<ScannedClass>,
    val conflicts: List<ClassConflict>
) {
    val hasConflicts: Boolean get() = conflicts.isNotEmpty()
    val classesScanned: Int get() = allClasses.size
}
