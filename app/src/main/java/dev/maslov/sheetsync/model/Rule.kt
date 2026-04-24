package dev.maslov.sheetsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey val id: UUID,
    val title: String,
    val description: String,
    val isActive: Boolean,
    val createdAt: Long,
    val sheetId: String,
    val lastRunStatus: String,
    val lastRunAt: Long
)