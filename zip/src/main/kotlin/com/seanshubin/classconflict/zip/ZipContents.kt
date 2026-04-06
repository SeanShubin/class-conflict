package com.seanshubin.classconflict.zip

import java.util.zip.ZipEntry

//
// This file was imported from: /Users/seashubi/github.com/SeanShubin/kotlin-reusable
// Module: zip
//
// Before editing this file, consider whether updating the source project
// and re-importing would be a better approach.
//

data class ZipContents(
    val path: List<String>,
    val zipEntry: ZipEntry,
    val bytes: List<Byte>
)
