package dev.maslov.sheetsync.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey val id: UUID,
    val title: String,
    val description: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val sheetId: String,
    @ColumnInfo(defaultValue = "")
    val sheetName: String,
    val lastRunStatus: String,
    val lastRunAt: LocalDateTime,
    @ColumnInfo(defaultValue = "none")
    val appId: String
)
