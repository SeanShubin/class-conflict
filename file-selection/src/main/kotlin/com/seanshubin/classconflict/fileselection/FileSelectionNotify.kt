package com.seanshubin.classconflict.fileselection

interface FileSelectionNotify {
    fun onInclude(relativePath: String, pattern: String)
    fun onExclude(relativePath: String, pattern: String)
    fun onUnmatched(relativePath: String)
    fun onSkipDirectory(relativePath: String, pattern: String)
}
