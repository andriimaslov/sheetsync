package dev.maslov.sheetsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isActive: Boolean,
    val createdAt: String,
    val sheetId: String,
    val lastRun: String
)