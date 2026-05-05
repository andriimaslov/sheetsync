package dev.maslov.sheetsync.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.maslov.sheetsync.model.AuthUser
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

object AuthPreferences {
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    private val KEY_USER_NAME = stringPreferencesKey("user_name")
    private val KEY_USER_PROFILE_PIC = stringPreferencesKey("user_profile_pic")

    suspend fun saveUserInfo(context: Context, userId: String, email: String, name: String, profilePicUrl: String?) {
        context.authDataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_USER_NAME] = name
            if (profilePicUrl != null) {
                preferences[KEY_USER_PROFILE_PIC] = profilePicUrl
            }
        }
    }

    fun getSavedUserInfo(context: Context) = context.authDataStore.data.map { preferences ->
        val userId = preferences[KEY_USER_ID]
        val email = preferences[KEY_USER_EMAIL]
        val name = preferences[KEY_USER_NAME]

        if (userId != null && email != null && name != null) {
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

    suspend fun clearAll(context: Context) {
        context.authDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
