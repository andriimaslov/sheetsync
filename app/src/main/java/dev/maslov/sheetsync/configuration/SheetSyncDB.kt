package dev.maslov.sheetsync.configuration

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.rules.RuleDao

@Database(
    version = 5,
    entities = [Rule::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = SheetSyncDB.DeleteTokenAndCredentialTablesSpec::class)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SheetSyncDB : RoomDatabase() {
    abstract fun ruleDao(): RuleDao

    @DeleteTable(tableName = "tokens")
    @DeleteTable(tableName = "client_credentials")
    class DeleteTokenAndCredentialTablesSpec : AutoMigrationSpec
}
