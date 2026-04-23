package dev.maslov.sheetsync.model

data class Rule(
    val id: String,
    val title: String,
    val description: String,
    val isActive: Boolean,
    val createdAt: String,
    val sheetId: String,
    val lastRun: String
)