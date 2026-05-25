package dev.maslov.sheetsync

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
@Suppress("ktlint:standard:property-naming")
class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.dataStore by preferencesDataStore(
        name = "app_preferences"
    )

    companion object {
        private val FIRST_LAUNCH =
            booleanPreferencesKey("first_launch")
    }

    val isFirstLaunchFlow: Flow<Boolean?> =
        context.dataStore.data.map { prefs ->
            prefs[FIRST_LAUNCH] ?: true
        }

    suspend fun completeOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH] = false
        }
    }
}
