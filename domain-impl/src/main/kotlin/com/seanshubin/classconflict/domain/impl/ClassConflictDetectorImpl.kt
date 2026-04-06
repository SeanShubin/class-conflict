package com.seanshubin.classconflict.domain.impl

import com.seanshubin.classconflict.domain.api.*
import java.nio.file.Path

class ClassConflictDetectorImpl(
    private val classScanner: ClassScanner
) : ClassConflictDetector {
    override fun detectConflicts(artifacts: List<Path>): ClassConflictReport {
        val allClasses = artifacts.flatMap { artifact ->
            classScanner.scanArtifact(artifact).map { scannedClass ->
                ClassInstance(artifact, scannedClass.hash) to scannedClass.fullyQualifiedName
            }
        }

        val groupedByName = allClasses.groupBy({ it.second }, { it.first })

        val conflicts = groupedByName
            .filter { (_, instances) -> instances.map { it.hash }.distinct().size > 1 }
            .map { (name, instances) ->
                ClassConflict(
                    fullyQualifiedName = name,
                    instances = instances.distinctBy { "${it.artifact}:${it.hash}" }
                )
            }
            .sortedBy { it.fullyQualifiedName }

        return ClassConflictReport(
            conflicts = conflicts,
            classesScanned = groupedByName.size
        )
    }
}
