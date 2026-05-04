package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

data class Configuration(
    val inputDir: Path,
    val outputDir: Path,
    val artifactFileRegexPatterns: RegexPatterns
)

data class RegexPatterns(
    val include: List<String>,
    val exclude: List<String>
)
