package dev.maslov.sheetsync

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.maslov.sheetsync.service.dao.RuleDao
import dev.maslov.sheetsync.service.RuleRepository
import dev.maslov.sheetsync.service.SheetSyncDB
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SheetSyncDB {
        return Room.databaseBuilder(
            context.applicationContext,
            SheetSyncDB::class.java,
            "sheetsync_db"
        ).build()
    }

    @Provides
    fun provideRuleDao(db: SheetSyncDB): RuleDao {
        return db.ruleDao()
    }

    @Provides
    fun provideRepository(dao: RuleDao): RuleRepository {
        return RuleRepository(dao)
    }
}