package dev.maslov.sheetsync.model

data class SheetMetadata(
    val id: String,
    val name: String,
    val mimeType: String = "application/vnd.google-apps.spreadsheet",
    val modifiedTime: String? = null
)
