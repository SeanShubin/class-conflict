package com.seanshubin.classconflict.domain.api

data class ClassConflict(
    val fullyQualifiedName: String,
    val instances: List<ClassInstance>
)
