package dev.maslov.sheetsync.configuration

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.maslov.sheetsync.service.googleapis.GoogleDriveApi
import dev.maslov.sheetsync.service.googleapis.GoogleSheetsApi
import dev.maslov.sheetsync.service.rules.RuleDao
import dev.maslov.sheetsync.service.token.GoogleAuthorizationInterceptor
import dev.maslov.sheetsync.service.token.ServiceAccountTokenProvider
import dev.maslov.sheetsync.service.token.TokenProvider
import jakarta.inject.Singleton
import javax.inject.Provider
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
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides
    fun provideTokenProvider(serviceAccountTokenProvider: ServiceAccountTokenProvider): TokenProvider =
        serviceAccountTokenProvider

    @Provides
    fun providesHttpClient(tokenProvider: Provider<TokenProvider>): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val oauth = GoogleAuthorizationInterceptor(tokenProvider)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(oauth)
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
