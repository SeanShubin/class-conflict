package com.seanshubin.classconflict.fileselection

import java.nio.file.Path

interface FileChooser {
    fun choose(fileSelection: FileSelection): List<Path>
}
