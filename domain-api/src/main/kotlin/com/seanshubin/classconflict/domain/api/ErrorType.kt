package com.seanshubin.classconflict.domain.api

enum class ErrorType(val caption: String, val isPartOfTotal: Boolean) {
    CLASSES_SCANNED("Classes Scanned", false),
    CONFLICTING_CLASSES("Conflicting Classes", true),
    CONFLICT_GROUPS("Conflict Groups", false);
}
