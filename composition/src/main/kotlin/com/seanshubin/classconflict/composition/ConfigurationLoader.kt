package com.seanshubin.classconflict.composition

import com.seanshubin.classconflict.composition.TypeUtil.coerceToListOfString
import com.seanshubin.classconflict.composition.TypeUtil.coerceToPath
import com.seanshubin.classconflict.di.contract.FilesContract
import com.seanshubin.classconflict.domain.api.Configuration
import com.seanshubin.classconflict.domain.api.RegexPatterns
import com.seanshubin.classconflict.dynamic.core.KeyValueStore
import com.seanshubin.classconflict.dynamic.core.KeyValueStoreWithDocumentation
import com.seanshubin.classconflict.dynamic.core.KeyValueStoreWithDocumentationDelegate
import com.seanshubin.classconflict.dynamic.json.JsonFileKeyValueStore
import java.nio.file.Path
import java.nio.file.Paths

class ConfigurationLoader(
    private val files: FilesContract,
    val configBaseName: String
) {
    fun load(): Configuration {
        val configFile = Paths.get("$configBaseName-config.json")
        val configDocumentationFile = Paths.get("$configBaseName-documentation.json")
        val keyValueStore: KeyValueStore = JsonFileKeyValueStore(files, configFile)
        val documentationKeyValueStore: KeyValueStore =
            JsonFileKeyValueStore(files, configDocumentationFile)
        val config: KeyValueStoreWithDocumentation =
            KeyValueStoreWithDocumentationDelegate(keyValueStore, documentationKeyValueStore)

        config.load(
            listOf("_documentation", "description"),
            "Scans Maven artifacts for class files with the same fully qualified name but different bytecode",
            listOf("Purpose: Detect class conflicts before they cause runtime issues")
        )
        config.load(
            listOf("_documentation", "readme"),
            "https://github.com/SeanShubin/class-conflict/blob/master/README.md",
            listOf("Documentation link")
        )
        config.load(
            listOf("_documentation", "configHelp"),
            "$configBaseName-documentation.json",
            listOf("Configuration help file")
        )

        val inputDir = config.load(
            listOf("inputDir"),
            ".",
            listOf(
                "Directory to scan for artifact files",
                "Scanned recursively for files matching include patterns",
                "Default: current working directory (.)"
            )
        ).coerceToPath()

        val outputDir = config.load(
            listOf("outputDir"),
            "generated/class-conflict",
            listOf("Directory for detailed reports (count, diff, browse)")
        ).coerceToPath()

        val artifactFileIncludeRegexPatterns: List<String> = config.load(
            listOf("artifactFileRegexPatterns", "include"),
            listOf(".*\\.jar", ".*\\.zip"),
            listOf(
                "Include only artifacts matching these regex patterns",
                "Default: .*\\.jar and .*\\.zip to match all JAR and ZIP files"
            )
        ).coerceToListOfString()

        val artifactFileExcludeRegexPatterns: List<String> = config.load(
            listOf("artifactFileRegexPatterns", "exclude"),
            emptyList<String>(),
            listOf(
                "Exclude artifacts matching these regex patterns",
                "Default: empty list (no exclusions)",
                "Example: [ \".*-sources\\.jar\", \".*-tests\\.jar\" ]"
            )
        ).coerceToListOfString()

        val artifactFileRegexPatterns = RegexPatterns(
            include = artifactFileIncludeRegexPatterns,
            exclude = artifactFileExcludeRegexPatterns
        )

        return Configuration(
            inputDir = inputDir,
            outputDir = outputDir,
            artifactFileRegexPatterns = artifactFileRegexPatterns
        )
    }
}
