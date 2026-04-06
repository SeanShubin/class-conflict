package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

data class Configuration(
    val artifacts: List<Path>,
    val outputDir: Path
)
