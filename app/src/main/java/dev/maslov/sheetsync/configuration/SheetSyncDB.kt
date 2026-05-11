package dev.maslov.sheetsync.configuration

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.maslov.sheetsync.model.ClientCredentials
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.model.Token
import dev.maslov.sheetsync.service.credentials.ClientCredentialsDao
import dev.maslov.sheetsync.service.rules.RuleDao
import dev.maslov.sheetsync.service.token.TokenDao

@Database(
    version = 4,
    entities = [Rule::class, ClientCredentials::class, Token::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SheetSyncDB : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun clientCredentialsDao(): ClientCredentialsDao
    abstract fun tokenDao(): TokenDao
}
