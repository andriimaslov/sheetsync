package dev.maslov.sheetsync.service

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.maslov.sheetsync.dao.RuleDao
import dev.maslov.sheetsync.model.Rule

@Database(entities = [Rule::class], version = 1)
abstract class SheetSyncDB: RoomDatabase() {
    abstract fun ruleDao(): RuleDao
}