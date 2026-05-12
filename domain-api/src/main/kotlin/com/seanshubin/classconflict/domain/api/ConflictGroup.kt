package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

data class ConflictGroup(
    val artifacts: List<Path>,
    val conflicts: List<ClassConflict>
)
