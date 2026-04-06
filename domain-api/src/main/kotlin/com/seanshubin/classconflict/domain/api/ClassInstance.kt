package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

data class ClassInstance(
    val artifact: Path,
    val hash: String
)
