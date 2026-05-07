package dev.maslov.sheetsync.configuration

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.maslov.sheetsync.model.ClientCredentials
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.credentials.ClientCredentialsDao
import dev.maslov.sheetsync.service.rules.RuleDao

@Database(
    version = 3,
    entities = [Rule::class, ClientCredentials::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SheetSyncDB : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun clientCredentialsDao(): ClientCredentialsDao
}
