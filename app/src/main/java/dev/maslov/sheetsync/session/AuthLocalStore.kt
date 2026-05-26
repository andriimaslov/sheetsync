package dev.maslov.sheetsync.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.maslov.sheetsync.model.AuthUser
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthLocalStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_PROFILE_PIC = stringPreferencesKey("user_profile_pic")
    }

    suspend fun saveUser(user: AuthUser) {
        context.authDataStore.edit { preferences ->
            preferences[KEY_USER_ID] = user.userId
            preferences[KEY_USER_EMAIL] = user.email
            preferences[KEY_USER_NAME] = user.name
            user.profilePicUrl?.let {
                preferences[KEY_USER_PROFILE_PIC] = it
            }
        }
    }
    suspend fun getSavedUserInfo(): AuthUser? {
        val preferences = context.authDataStore.data.map { it }.first()
        val userId = preferences[KEY_USER_ID]
        val email = preferences[KEY_USER_EMAIL]
        val name = preferences[KEY_USER_NAME]

        return if (userId != null && email != null && name != null) {
            AuthUser(
                userId = userId,
                email = email,
                name = name,
                profilePicUrl = preferences[KEY_USER_PROFILE_PIC]
            )
        } else {
            null
        }
    }
    suspend fun clearAll() {
        context.authDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
