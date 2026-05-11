package dev.maslov.sheetsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tokens")
data class Token(
    @PrimaryKey(autoGenerate = false)
    val id: UUID = UUID.randomUUID(),
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtMillis: Long
) {
    companion object {
        fun fromResponse(accessToken: String, refreshToken: String, expiresInSeconds: Long): Token = Token(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtMillis = System.currentTimeMillis() + expiresInSeconds * 1000
        )
    }
}
