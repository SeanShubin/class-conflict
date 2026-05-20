package com.seanshubin.classconflict.domain.api

import java.nio.file.Path

data class ClassConflictReport(
    val configuration: Configuration,
    val artifacts: List<Path>,
    val allClasses: List<ScannedClass>,
    val conflicts: List<ClassConflict>
) {
    val hasConflicts: Boolean get() = conflicts.isNotEmpty()
    val classesScanned: Int get() = allClasses.size
    val conflictingVersionsFound: Int
        get() = conflicts.sumOf { conflict -> conflict.instances.map { it.hash }.distinct().size }
    val summary: Summary
        get() = Summary(
            mapOf(
                ErrorType.CLASSES_SCANNED to ErrorSummaryItem(classesScanned, ErrorType.CLASSES_SCANNED.isPartOfTotal),
                ErrorType.CONFLICTING_CLASSES to ErrorSummaryItem(conflicts.size, ErrorType.CONFLICTING_CLASSES.isPartOfTotal),
                ErrorType.CONFLICT_GROUPS to ErrorSummaryItem(conflictGroups.size, ErrorType.CONFLICT_GROUPS.isPartOfTotal)
            ),
            configuration.errorLimit
        )
    val conflictGroups: List<ConflictGroup>
        get() = conflicts
            .groupBy { conflict ->
                conflict.instances.map { it.artifact }.distinct().sortedBy { it.toString() }
            }
            .map { (artifacts, conflictsInGroup) ->
                ConflictGroup(
                    artifacts = artifacts,
                    conflicts = conflictsInGroup
                        .sortedBy { it.fullyQualifiedName }
                        .map { conflict ->
                            conflict.copy(
                                instances = conflict.instances.sortedWith(
                                    compareBy({ it.hash }, { it.artifact.toString() })
                                )
                            )
                        }
                )
            }
            .sortedWith(
                compareByDescending<ConflictGroup> { it.conflicts.size }
                    .thenBy { it.artifacts.first().toString() }
            )
}
