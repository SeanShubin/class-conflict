package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

interface ClassScanner {
    fun scanArtifact(artifact: Path): List<ScannedClass>
}
