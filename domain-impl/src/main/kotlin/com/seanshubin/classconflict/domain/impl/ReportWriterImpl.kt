package com.seanshubin.classconflict.domain.impl

import com.seanshubin.classconflict.domain.api.ClassConflict
import com.seanshubin.classconflict.domain.api.ClassConflictReport
import com.seanshubin.classconflict.domain.api.ReportWriter
import com.seanshubin.classconflict.domain.api.ScannedClass
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
            appendLine("  \"conflictsFound\" : ${report.conflicts.size}")
            appendLine("}")
        }

        val countFile = countDir.resolve("quality-metrics.json")
        Files.writeString(countFile, json)
    }

    private fun writeDiffReports(report: ClassConflictReport, outputDir: Path) {
        val diffDir = outputDir.resolve("diff")
        Files.createDirectories(diffDir)

        for (conflict in report.conflicts) {
            val json = formatConflictAsJson(conflict)
            val fileName = "conflict-${conflict.fullyQualifiedName}.json"
            val diffFile = diffDir.resolve(fileName)
            Files.writeString(diffFile, json)
        }
    }

    private fun writeBrowseReports(report: ClassConflictReport, outputDir: Path) {
        val browseDir = outputDir.resolve("browse")
        Files.createDirectories(browseDir)

        writeStaticContent(browseDir)
        writeIndexPage(report, browseDir)
        writeArtifactsPage(report, browseDir)
        writeClassesPage(report, browseDir)
        writeConflictsSummaryPage(report, browseDir)
        writeConflictDetailPages(report, browseDir)
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
                    Tag("tr", Tag("td", Text("Conflicts Found")), Tag("td", Text("${report.conflicts.size}")))
                )
            ),
            Tag("h2", Text("Reports")),
            Tag(
                "div",
                listOf(
                    HtmlUtil.anchor("Artifacts (${report.artifacts.size})", "artifacts.html"),
                    HtmlUtil.anchor("Classes (${report.classesScanned})", "classes.html"),
                    HtmlUtil.anchor("Conflicts (${report.conflicts.size})", "conflicts.html")
                ),
                listOf("class" to "column-1")
            )
        )

        val html = createHtmlPage("Class Conflict Report", body)
        Files.write(browseDir.resolve("index.html"), html.toLines())
    }

    private fun writeArtifactsPage(report: ClassConflictReport, browseDir: Path) {
        val body = Tag(
            "body",
            Tag("h1", Text("Artifacts")),
            Tag("p", HtmlUtil.anchor("Table Of Contents", "index.html")),
            Tag("p", Text("artifact count: ${report.artifacts.size}")),
            *HtmlUtil.createTableWithText(
                report.artifacts,
                listOf("Artifact Path"),
                { artifact -> listOf(artifact.toString()) }
            ).toTypedArray()
        )

        val html = createHtmlPage("Artifacts", body)
        Files.write(browseDir.resolve("artifacts.html"), html.toLines())
    }

    private fun writeClassesPage(report: ClassConflictReport, browseDir: Path) {
        val body = Tag(
            "body",
            Tag("h1", Text("Classes")),
            Tag("p", HtmlUtil.anchor("Table Of Contents", "index.html")),
            Tag("p", Text("class count: ${report.allClasses.size}")),
            *HtmlUtil.createTableWithText(
                report.allClasses,
                listOf("Fully Qualified Name", "Artifact", "SHA-256 Hash"),
                { scannedClass ->
                    listOf(
                        scannedClass.fullyQualifiedName,
                        scannedClass.artifact.fileName.toString(),
                        scannedClass.hash
                    )
                }
            ).toTypedArray()
        )

        val html = createHtmlPage("Classes", body)
        Files.write(browseDir.resolve("classes.html"), html.toLines())
    }

    private fun writeConflictsSummaryPage(report: ClassConflictReport, browseDir: Path) {
        val body = if (report.hasConflicts) {
            Tag(
                "body",
                Tag("h1", Text("Conflicts")),
                Tag("p", HtmlUtil.anchor("Table Of Contents", "index.html")),
                Tag("p", Text("conflict count: ${report.conflicts.size}")),
                *HtmlUtil.createTableWithElements(
                    report.conflicts,
                    listOf("Fully Qualified Name", "Versions"),
                    { conflict ->
                        listOf(
                            HtmlUtil.anchor(
                                conflict.fullyQualifiedName,
                                "conflict-${conflict.fullyQualifiedName}.html"
                            ),
                            Text("${conflict.instances.size}")
                        )
                    }
                ).toTypedArray()
            )
        } else {
            Tag(
                "body",
                Tag("h1", Text("Conflicts")),
                Tag("p", HtmlUtil.anchor("Table Of Contents", "index.html")),
                Tag("p", Text("No conflicts detected!"))
            )
        }

        val html = createHtmlPage("Conflicts", body)
        Files.write(browseDir.resolve("conflicts.html"), html.toLines())
    }

    private fun writeConflictDetailPages(report: ClassConflictReport, browseDir: Path) {
        for (conflict in report.conflicts) {
            val body = Tag(
                "body",
                Tag("h1", Text("Conflict: ${conflict.fullyQualifiedName}")),
                Tag("p", HtmlUtil.anchor("Conflicts", "conflicts.html")),
                Tag("p", Text("This class appears in ${conflict.instances.size} different versions:")),
                *HtmlUtil.createTableWithText(
                    conflict.instances,
                    listOf("Artifact", "SHA-256 Hash"),
                    { instance ->
                        listOf(
                            instance.artifact.toString(),
                            instance.hash
                        )
                    }
                ).toTypedArray(),
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

            val html = createHtmlPage("Conflict: ${conflict.fullyQualifiedName}", body)
            val fileName = "conflict-${conflict.fullyQualifiedName}.html"
            Files.write(browseDir.resolve(fileName), html.toLines())
        }
    }

    private fun formatConflictAsJson(conflict: ClassConflict): String {
        return buildString {
            appendLine("{")
            appendLine("  \"fullyQualifiedName\" : \"${conflict.fullyQualifiedName}\",")
            appendLine("  \"instances\" : [")
            conflict.instances.forEachIndexed { index, instance ->
                appendLine("    {")
                appendLine("      \"artifact\" : \"${instance.artifact.fileName}\",")
                appendLine("      \"hash\" : \"${instance.hash}\"")
                append("    }")
                if (index < conflict.instances.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ]")
            appendLine("}")
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
