package dev.maslov.sheetsync.service

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.maslov.sheetsync.Converters
import dev.maslov.sheetsync.dao.RuleDao
import dev.maslov.sheetsync.model.Rule

@Database(entities = [Rule::class], version = 1)
@TypeConverters(Converters::class)
abstract class SheetSyncDB: RoomDatabase() {
    abstract fun ruleDao(): RuleDao
}