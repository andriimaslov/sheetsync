package dev.maslov.sheetsync.configuration

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.rules.RuleDao

@Database(
    version = 1,
    entities = [Rule::class],
    autoMigrations = [],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SheetSyncDB : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
}
