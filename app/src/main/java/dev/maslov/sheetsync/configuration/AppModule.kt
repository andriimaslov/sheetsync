package dev.maslov.sheetsync.configuration

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.maslov.sheetsync.BuildConfig
import dev.maslov.sheetsync.service.credentials.ClientCredentialsDao
import dev.maslov.sheetsync.service.credentials.ClientCredentialsRepository
import dev.maslov.sheetsync.service.rules.RuleDao
import dev.maslov.sheetsync.service.rules.RuleRepository
import dev.maslov.sheetsync.service.token.GoogleSheetsAuthorizationManager
import dev.maslov.sheetsync.service.token.GoogleTokenExchangeService
import dev.maslov.sheetsync.service.token.TokenDao
import dev.maslov.sheetsync.service.token.TokenRepository
import dev.maslov.sheetsync.session.AuthLocalStore
import dev.maslov.sheetsync.session.AuthRepository
import dev.maslov.sheetsync.session.GoogleAuthClient
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
    fun provideClientCredentialsDao(db: SheetSyncDB): ClientCredentialsDao = db.clientCredentialsDao()

    @Provides
    fun provideRepository(dao: RuleDao): RuleRepository = RuleRepository(dao)

    @Provides
    fun provideClientCredentialsRepository(dao: ClientCredentialsDao): ClientCredentialsRepository =
        ClientCredentialsRepository(dao)

    @Provides
    @Singleton
    fun provideAuthRepository(googleAuthClient: GoogleAuthClient, localStore: AuthLocalStore): AuthRepository =
        AuthRepository(googleAuthClient, localStore)

    @Provides
    fun provideGoogleAuthClient(@ApplicationContext applicationContext: Context): GoogleAuthClient =
        GoogleAuthClient(applicationContext, BuildConfig.OAUTH_CLIENT_ID)

    @Provides
    fun provideRuleViewModel(repository: RuleRepository) = RuleViewModel(repository)

    @Provides
    fun provideTokenRepository(dao: TokenDao): TokenRepository = TokenRepository(dao)

    @Provides
    fun provideTokenDao(db: SheetSyncDB): TokenDao = db.tokenDao()

    @Provides
    fun provideGoogleSheetsAuthorizationManager(@ApplicationContext context: Context) =
        GoogleSheetsAuthorizationManager(context, BuildConfig.OAUTH_CLIENT_ID)

    @Provides
    fun provideGoogleTokenExchangeService(credentialsRepository: ClientCredentialsRepository) =
        GoogleTokenExchangeService(credentialsRepository)
}
