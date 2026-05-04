package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

interface ClassConflictDetector {
    fun detectConflicts(configuration: Configuration, artifacts: List<Path>): ClassConflictReport
}
