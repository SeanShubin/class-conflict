package com.seanshubin.classconflict.domain.impl

import com.seanshubin.classconflict.domain.api.ClassScanner
import com.seanshubin.classconflict.domain.api.ScannedClass
import com.seanshubin.classconflict.zip.ZipContentsIterator
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class ClassScannerImpl : ClassScanner {
    override fun scanArtifact(artifact: Path): List<ScannedClass> {
        if (!Files.exists(artifact)) return emptyList()
        if (!isZipFile(artifact)) return emptyList()

        val classes = mutableListOf<ScannedClass>()
        Files.newInputStream(artifact).use { inputStream ->
            val iterator = ZipContentsIterator(
                inputStream = inputStream,
                name = artifact.fileName.toString(),
                isZip = { name -> name.endsWith(".jar") || name.endsWith(".zip") },
                accept = { _, entry -> !entry.isDirectory && entry.name.endsWith(".class") }
            )

            for (zipContents in iterator) {
                val fullyQualifiedName = extractClassName(zipContents.zipEntry.name)
                val hash = computeHash(zipContents.bytes)
                classes.add(ScannedClass(fullyQualifiedName, hash))
            }
        }
        return classes
    }

    private fun isZipFile(path: Path): Boolean {
        val fileName = path.fileName.toString()
        return fileName.endsWith(".jar") || fileName.endsWith(".zip")
    }

    private fun extractClassName(entryName: String): String {
        return entryName
            .removeSuffix(".class")
            .replace('/', '.')
    }

    private fun computeHash(bytes: List<Byte>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val byteArray = bytes.toByteArray()
        val hashBytes = digest.digest(byteArray)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
