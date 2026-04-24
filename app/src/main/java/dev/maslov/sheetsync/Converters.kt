package dev.maslov.sheetsync

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuidString: String?): UUID? = uuidString?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromLocalDateTime(localDateTime: LocalDateTime): String = localDateTime.toString()

    @TypeConverter
    fun toLocaleDateTime(localDateTimeString: String): LocalDateTime = LocalDateTime.parse(localDateTimeString)
}