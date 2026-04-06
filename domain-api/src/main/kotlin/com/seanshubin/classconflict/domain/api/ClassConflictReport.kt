package com.seanshubin.classconflict.domain.api

data class ClassConflictReport(
    val conflicts: List<ClassConflict>,
    val classesScanned: Int
) {
    val hasConflicts: Boolean get() = conflicts.isNotEmpty()
}
