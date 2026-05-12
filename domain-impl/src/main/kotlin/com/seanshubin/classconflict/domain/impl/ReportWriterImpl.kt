package com.seanshubin.classconflict.domain.impl

import com.seanshubin.classconflict.domain.api.ClassConflictReport
import com.seanshubin.classconflict.domain.api.ConflictGroup
import com.seanshubin.classconflict.domain.api.ReportWriter
import com.seanshubin.classconflict.html.HtmlElement
import com.seanshubin.classconflict.html.HtmlElement.Tag
import com.seanshubin.classconflict.html.HtmlElement.Text
import com.seanshubin.classconflict.html.HtmlUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ReportWriterImpl : ReportWriter {
    private val classLoader = javaClass.classLoader

    override fun writeReports(report: ClassConflictReport, outputDir: Path) {
        writeCountReport(report, outputDir)
        writeDiffReports(report, outputDir)
        writeBrowseReports(report, outputDir)
    }

    private fun writeCountReport(report: ClassConflictReport, outputDir: Path) {
        val countDir = outputDir.resolve("count")
        Files.createDirectories(countDir)

        val json = buildString {
            appendLine("{")
            appendLine("  \"classesScanned\" : ${report.classesScanned},")
            appendLine("  \"conflictGroupsFound\" : ${report.conflictGroups.size},")
            appendLine("  \"conflictingVersionsFound\" : ${report.conflictingVersionsFound}")
            appendLine("}")
        }

        val countFile = countDir.resolve("quality-metrics.json")
        Files.writeString(countFile, json)
    }

    private fun writeDiffReports(report: ClassConflictReport, outputDir: Path) {
        val diffDir = outputDir.resolve("diff")
        Files.createDirectories(diffDir)
        val json = formatGroupsAsJson(report.conflictGroups)
        Files.writeString(diffDir.resolve("quality-metrics-conflictGroups.json"), json)
    }

    private data class ArtifactStats(val artifact: Path, val groupCount: Int, val classCount: Int)

    private fun writeBrowseReports(report: ClassConflictReport, outputDir: Path) {
        val browseDir = outputDir.resolve("browse")
        Files.createDirectories(browseDir)

        val groupsByArtifact: Map<Path, List<ConflictGroup>> = report.conflictGroups
            .flatMap { group -> group.artifacts.map { artifact -> artifact to group } }
            .groupBy({ it.first }, { it.second })

        val sortedStats: List<ArtifactStats> = report.artifacts.map { artifact ->
            val groups = groupsByArtifact[artifact] ?: emptyList()
            ArtifactStats(artifact, groups.size, groups.sumOf { it.conflicts.size })
        }.sortedWith(
            compareByDescending<ArtifactStats> { it.groupCount }
                .thenByDescending { it.classCount }
                .thenBy { it.artifact.toString() }
        )

        val artifactIndex: Map<Path, Int> = sortedStats
            .filter { it.groupCount > 0 }
            .mapIndexed { i, s -> s.artifact to (i + 1) }
            .toMap()

        val groupNumbers: Map<ConflictGroup, Int> = report.conflictGroups
            .mapIndexed { i, group -> group to (i + 1) }
            .toMap()

        writeStaticContent(browseDir)
        writeIndexRedirect(browseDir)
        writeIndexPage(report, browseDir)
        writeArtifactsPage(sortedStats, artifactIndex, browseDir)
        writeClassVersionsPage(report, browseDir)
        writeConflictsSummaryPage(report, browseDir)
        writeConflictDetailPages(report, browseDir)
        writeArtifactDetailPages(artifactIndex, groupsByArtifact, groupNumbers, browseDir)
    }

    private fun writeIndexRedirect(browseDir: Path) {
        val content = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta http-equiv="refresh" content="0; url=index.html">
                <title>Redirecting...</title>
            </head>
            <body>
            <p>Redirecting to <a href="index.html">index.html</a>...</p>
            </body>
            </html>
        """.trimIndent()
        Files.writeString(browseDir.resolve("_index.html"), content)
    }

    private fun writeStaticContent(browseDir: Path) {
        val cssFiles = listOf("reset.css", "class-conflict.css")
        for (cssFile in cssFiles) {
            val resourceName = "static-content/$cssFile"
            val inputStream = classLoader.getResourceAsStream(resourceName)
                ?: throw RuntimeException("Unable to load resource named '$resourceName'")
            val content = inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
            Files.writeString(browseDir.resolve(cssFile), content)
        }
    }

    private fun writeIndexPage(report: ClassConflictReport, browseDir: Path) {
        val config = report.configuration
        val body = Tag(
            "body",
            Tag("h1", Text("Class Conflict Report")),
            Tag("h2", Text("Configuration")),
            Tag(
                "table",
                Tag("thead", Tag("tr", Tag("th", Text("Setting")), Tag("th", Text("Value")))),
                Tag(
                    "tbody",
                    Tag("tr", Tag("td", Text("Input Directory")), Tag("td", Text(config.inputDir.toString()))),
                    Tag("tr", Tag("td", Text("Output Directory")), Tag("td", Text(config.outputDir.toString()))),
                    Tag(
                        "tr",
                        Tag("td", Text("Include Patterns")),
                        Tag("td", Text(config.artifactFileRegexPatterns.include.joinToString(", ")))
                    ),
                    Tag(
                        "tr",
                        Tag("td", Text("Exclude Patterns")),
                        Tag("td", Text(config.artifactFileRegexPatterns.exclude.joinToString(", ")))
                    )
                )
            ),
            Tag("h2", Text("Summary")),
            Tag(
                "table",
                Tag("thead", Tag("tr", Tag("th", Text("Metric")), Tag("th", Text("Count")))),
                Tag(
                    "tbody",
                    Tag("tr", Tag("td", Text("Artifacts Scanned")), Tag("td", Text("${report.artifacts.size}"))),
                    Tag("tr", Tag("td", Text("Classes Scanned")), Tag("td", Text("${report.classesScanned}"))),
                    Tag("tr", Tag("td", Text("Conflict Groups Found")), Tag("td", Text("${report.conflictGroups.size}"))),
                    Tag("tr", Tag("td", Text("Conflicting Versions Found")), Tag("td", Text("${report.conflictingVersionsFound}")))
                )
            ),
            Tag("h2", Text("Reports")),
            Tag(
                "div",
                listOf(
                    HtmlUtil.anchor("Artifacts (${report.artifacts.size})", "artifacts.html"),
                    HtmlUtil.anchor("Class Versions (${report.conflicts.size})", "class-versions.html"),
                    HtmlUtil.anchor("Conflict Groups (${report.conflictGroups.size})", "conflicts.html")
                ),
                listOf("class" to "column-1")
            )
        )

        val html = createHtmlPage("Class Conflict Report", body)
        Files.write(browseDir.resolve("index.html"), html.toLines())
    }

    private fun writeArtifactsPage(
        sortedStats: List<ArtifactStats>,
        artifactIndex: Map<Path, Int>,
        browseDir: Path
    ) {
        val body = Tag(
            "body",
            Tag("h1", Text("Artifacts")),
            Tag("p", HtmlUtil.anchor("Table Of Contents", "index.html")),
            Tag("p", Text("artifact count: ${sortedStats.size}")),
            *HtmlUtil.createTableWithElements(
                sortedStats,
                listOf("Artifact", "Conflict Groups", "Conflicting Classes"),
                { stats ->
                    val artifactCell = artifactIndex[stats.artifact]?.let { idx ->
                        HtmlUtil.anchor(stats.artifact.toString(), "artifact-$idx.html")
                    } ?: Text(stats.artifact.toString())
                    listOf(
                        artifactCell,
                        Text("${stats.groupCount}"),
                        Text("${stats.classCount}")
                    )
                }
            ).toTypedArray()
        )

        val html = createHtmlPage("Artifacts", body)
        Files.write(browseDir.resolve("artifacts.html"), html.toLines())
    }

    private fun writeArtifactDetailPages(
        artifactIndex: Map<Path, Int>,
        groupsByArtifact: Map<Path, List<ConflictGroup>>,
        groupNumbers: Map<ConflictGroup, Int>,
        browseDir: Path
    ) {
        for ((artifact, idx) in artifactIndex) {
            val groups = (groupsByArtifact[artifact] ?: emptyList())
                .sortedBy { groupNumbers[it]!! }
            val body = Tag(
                "body",
                Tag("h1", Text("Artifact: $artifact")),
                Tag("p", HtmlUtil.anchor("Artifacts", "artifacts.html")),
                Tag("p", Text("conflict groups: ${groups.size}")),
                *HtmlUtil.createTableWithElements(
                    groups,
                    listOf("Conflict Group", "Conflicting Classes"),
                    { group ->
                        val groupNum = groupNumbers[group]!!
                        listOf(
                            HtmlUtil.anchor("Group $groupNum", "conflict-group-$groupNum.html"),
                            Text("${group.conflicts.size}")
                        )
                    }
                ).toTypedArray()
            )

            val html = createHtmlPage("Artifact: $artifact", body)
            Files.write(browseDir.resolve("artifact-$idx.html"), html.toLines())
        }
    }

    private fun writeClassVersionsPage(report: ClassConflictReport, browseDir: Path) {
        data class ClassVersionCount(val className: String, val versionCount: Int, val groupNumber: Int)

        val classToGroupNumber: Map<String, Int> = report.conflictGroups
            .flatMapIndexed { index, group ->
                group.conflicts.map { conflict -> conflict.fullyQualifiedName to (index + 1) }
            }
            .toMap()

        val rows = report.conflicts
            .map { conflict ->
                ClassVersionCount(
                    conflict.fullyQualifiedName,
                    conflict.instances.map { it.hash }.distinct().size,
                    classToGroupNumber.getValue(conflict.fullyQualifiedName)
                )
            }
            .sortedWith(
                compareByDescending<ClassVersionCount> { it.versionCount }
                    .thenBy { it.className }
            )

        val body = Tag(
            "body",
            Tag("h1", Text("Class Versions")),
            Tag("p", HtmlUtil.anchor("Table Of Contents", "index.html")),
            Tag("p", Text("conflicting class count: ${rows.size}")),
            *HtmlUtil.createTableWithElements(
                rows,
                listOf("Class", "Versions", "Conflict Group"),
                { row ->
                    listOf(
                        Text(row.className),
                        Text("${row.versionCount}"),
                        HtmlUtil.anchor("Group ${row.groupNumber}", "conflict-group-${row.groupNumber}.html")
                    )
                }
            ).toTypedArray()
        )

        val html = createHtmlPage("Class Versions", body)
        Files.write(browseDir.resolve("class-versions.html"), html.toLines())
    }

    private fun writeConflictsSummaryPage(report: ClassConflictReport, browseDir: Path) {
        val body = if (report.hasConflicts) {
            Tag(
                "body",
                Tag("h1", Text("Conflict Groups")),
                Tag("p", HtmlUtil.anchor("Table Of Contents", "index.html")),
                Tag("p", Text("conflict group count: ${report.conflictGroups.size}")),
                *HtmlUtil.createTableWithElements(
                    report.conflictGroups.mapIndexed { index, group -> index + 1 to group },
                    listOf("Group", "Classes", "Archives"),
                    { (index, group) ->
                        listOf(
                            HtmlUtil.anchor("Group $index", "conflict-group-$index.html"),
                            Text("${group.conflicts.size}"),
                            Text("${group.artifacts.size}")
                        )
                    }
                ).toTypedArray()
            )
        } else {
            Tag(
                "body",
                Tag("h1", Text("Conflict Groups")),
                Tag("p", HtmlUtil.anchor("Table Of Contents", "index.html")),
                Tag("p", Text("No conflicts detected!"))
            )
        }

        val html = createHtmlPage("Conflict Groups", body)
        Files.write(browseDir.resolve("conflicts.html"), html.toLines())
    }

    private fun writeConflictDetailPages(report: ClassConflictReport, browseDir: Path) {
        for ((index, group) in report.conflictGroups.withIndex()) {
            val groupNumber = index + 1
            val body = Tag(
                "body",
                Tag("h1", Text("Conflict Group $groupNumber")),
                Tag("p", HtmlUtil.anchor("Conflict Groups", "conflicts.html")),
                Tag("h2", Text("Archives (${group.artifacts.size})")),
                *HtmlUtil.createTableWithText(
                    group.artifacts,
                    listOf("Artifact Path"),
                    { artifact -> listOf(artifact.toString()) }
                ).toTypedArray(),
                Tag("h2", Text("Classes (${group.conflicts.size})")),
                *group.conflicts.flatMap { conflict ->
                    listOf(Tag("h3", Text(conflict.fullyQualifiedName))) +
                        HtmlUtil.createTableWithText(
                            conflict.instances,
                            listOf("Archive", "CRC-32 Hash"),
                            { instance -> listOf(instance.artifact.toString(), instance.hash) }
                        )
                }.toTypedArray(),
                Tag("h2", Text("Impact")),
                Tag(
                    "p",
                    Text(
                        "The JVM will load whichever version it encounters first, leading to " +
                                "unpredictable behavior. This can cause:"
                    )
                ),
                HtmlUtil.listItems(
                    listOf(
                        "Method not found exceptions at runtime",
                        "Unexpected behavior due to different implementations",
                        "ClassCastException errors"
                    )
                ),
                Tag("h2", Text("Resolution")),
                HtmlUtil.orderedListItems(
                    listOf(
                        "Determine which version is correct for your application",
                        "Use dependency management to exclude the unwanted versions",
                        "Consider using a dependency convergence strategy"
                    )
                )
            )

            val html = createHtmlPage("Conflict Group $groupNumber", body)
            val fileName = "conflict-group-$groupNumber.html"
            Files.write(browseDir.resolve(fileName), html.toLines())
        }
    }

    private fun formatGroupsAsJson(groups: List<ConflictGroup>): String {
        return buildString {
            appendLine("[")
            groups.forEachIndexed { groupIndex, group ->
                appendLine("  {")
                appendLine("    \"artifacts\" : [")
                group.artifacts.forEachIndexed { index, artifact ->
                    append("      \"$artifact\"")
                    if (index < group.artifacts.size - 1) appendLine(",") else appendLine()
                }
                appendLine("    ],")
                appendLine("    \"classNames\" : [")
                group.conflicts.forEachIndexed { index, conflict ->
                    append("      \"${conflict.fullyQualifiedName}\"")
                    if (index < group.conflicts.size - 1) appendLine(",") else appendLine()
                }
                append("    ]")
                appendLine()
                append("  }")
                if (groupIndex < groups.size - 1) appendLine(",") else appendLine()
            }
            append("]")
        }
    }

    private fun createHtmlPage(title: String, body: HtmlElement): HtmlElement {
        return Tag(
            "html",
            Tag(
                "head",
                Tag("title", Text(title)),
                Tag("link", attributes = listOf("rel" to "stylesheet", "href" to "reset.css")),
                Tag("link", attributes = listOf("rel" to "stylesheet", "href" to "class-conflict.css"))
            ),
            body
        )
    }
}
