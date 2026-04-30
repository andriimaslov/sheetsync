package dev.maslov.sheetsync.service

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.maslov.sheetsync.Converters
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.dao.RuleDao

@Database(
    version = 2,
    entities = [Rule::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SheetSyncDB : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
}
