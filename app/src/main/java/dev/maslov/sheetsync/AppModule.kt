package dev.maslov.sheetsync

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.maslov.sheetsync.service.RuleRepository
import dev.maslov.sheetsync.service.SheetSyncDB
import dev.maslov.sheetsync.service.dao.RuleDao
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SheetSyncDB = Room
        .databaseBuilder(
            context.applicationContext,
            SheetSyncDB::class.java,
            "sheetsync_db"
        ).build()

    @Provides
    fun provideRuleDao(db: SheetSyncDB): RuleDao = db.ruleDao()

    @Provides
    fun provideRepository(dao: RuleDao): RuleRepository = RuleRepository(dao)

    @Provides
    fun provideRuleViewModel(repository: RuleRepository) = RuleViewModel(repository)
}
