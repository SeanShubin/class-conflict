package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

data class ScannedClass(
    val fullyQualifiedName: String,
    val artifact: Path,
    val hash: String
)
