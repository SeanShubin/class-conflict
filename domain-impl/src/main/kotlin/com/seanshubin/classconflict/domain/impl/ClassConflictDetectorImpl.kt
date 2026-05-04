package com.seanshubin.classconflict.domain.impl

import com.seanshubin.classconflict.domain.api.*
import java.nio.file.Path

class ClassConflictDetectorImpl(
    private val classScanner: ClassScanner
) : ClassConflictDetector {
    override fun detectConflicts(configuration: Configuration, artifacts: List<Path>): ClassConflictReport {
        val scannedClasses = artifacts.flatMap { artifact ->
            classScanner.scanArtifact(artifact).map { scannedClass ->
                ScannedClass(
                    fullyQualifiedName = scannedClass.fullyQualifiedName,
                    artifact = artifact,
                    hash = scannedClass.hash
                )
            }
        }.sortedBy { it.fullyQualifiedName }

        val groupedByName = scannedClasses.groupBy { it.fullyQualifiedName }

        val conflicts = groupedByName
            .filter { (_, instances) -> instances.map { it.hash }.distinct().size > 1 }
            .map { (name, instances) ->
                ClassConflict(
                    fullyQualifiedName = name,
                    instances = instances.map {
                        ClassInstance(it.artifact, it.hash)
                    }.distinctBy { "${it.artifact}:${it.hash}" }
                )
            }
            .sortedBy { it.fullyQualifiedName }

        return ClassConflictReport(
            configuration = configuration,
            artifacts = artifacts,
            allClasses = scannedClasses,
            conflicts = conflicts
        )
    }
}
