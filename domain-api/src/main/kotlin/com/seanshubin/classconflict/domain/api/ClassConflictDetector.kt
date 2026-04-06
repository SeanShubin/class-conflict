package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

interface ClassConflictDetector {
    fun detectConflicts(artifacts: List<Path>): ClassConflictReport
}
