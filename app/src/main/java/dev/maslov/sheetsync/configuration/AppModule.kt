package dev.maslov.sheetsync.configuration

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.maslov.sheetsync.BuildConfig
import dev.maslov.sheetsync.service.googleapis.GoogleDriveApi
import dev.maslov.sheetsync.service.googleapis.GoogleSheetsApi
import dev.maslov.sheetsync.service.rules.RuleDao
import dev.maslov.sheetsync.service.rules.RuleRepository
import dev.maslov.sheetsync.service.token.AuthorizationManager
import dev.maslov.sheetsync.session.AuthLocalStore
import dev.maslov.sheetsync.session.AuthRepository
import dev.maslov.sheetsync.session.AuthRequirementManager
import dev.maslov.sheetsync.session.GoogleAuthClient
import dev.maslov.sheetsync.session.OAuthCredManager
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    @Singleton
    fun provideAuthRepository(googleAuthClient: GoogleAuthClient, localStore: AuthLocalStore): AuthRepository =
        AuthRepository(googleAuthClient, localStore)

    @Provides
    fun provideGoogleAuthClient(@ApplicationContext applicationContext: Context): GoogleAuthClient =
        GoogleAuthClient(applicationContext, BuildConfig.OAUTH_CLIENT_ID)

    @Provides
    fun provideRuleViewModel(repository: RuleRepository) = RuleViewModel(repository)

    @Provides
    fun provideGoogleSheetsAuthorizationManager(
        @ApplicationContext context: Context,
        oAuthCredManager: OAuthCredManager,
        authRequirementManager: AuthRequirementManager
    ) = AuthorizationManager(context, BuildConfig.OAUTH_CLIENT_ID, oAuthCredManager, authRequirementManager)

    @Provides
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides
    fun providesHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    fun providesGoogleDriveApi(httpClient: OkHttpClient): GoogleDriveApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(GoogleDriveApi::class.java)
    }

    @Provides
    fun providesGoogleSheetsApi(httpClient: OkHttpClient): GoogleSheetsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://sheets.googleapis.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(GoogleSheetsApi::class.java)
    }
}
