package dev.maslov.sheetsync.session

import androidx.datastore.core.DataStore
import dev.maslov.sheetsync.model.OAuthConfiguration
import dev.maslov.sheetsync.model.OAuthCreds
import dev.maslov.sheetsync.model.OAuthToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthCredManager @Inject constructor(private val dataStore: DataStore<OAuthConfiguration>) {

    // Expose the entire state reactively
    val credentialsFlow: Flow<OAuthConfiguration> = dataStore.data

    // Check if BYOC setup is complete
    val isConfiguredFlow: Flow<Boolean> = dataStore.data.map {
        !it.oAuthCreds?.clientId.isNullOrBlank() && it.oAuthCreds.clientSecret.isNotBlank()
    }

    // Single-shot read for network interceptors
    suspend fun getCredentialsSync(): OAuthConfiguration = dataStore.data.first()

    suspend fun getTokenSync(): OAuthToken? = dataStore.data.first().oAuthToken

    fun isTokenExpired(token: OAuthToken): Boolean = token.expiresAtMillis < System.currentTimeMillis()

    suspend fun saveCredentials(clientId: String, clientSecret: String) {
        dataStore.updateData { current ->
            current.copy(
                oAuthCreds = OAuthCreds(clientId, clientSecret)
            )
        }
    }

    suspend fun saveToken(accessToken: String, refreshToken: String?, expiryTimestamp: Long) {
        dataStore.updateData { current ->
            current.copy(
                oAuthToken = OAuthToken.fromResponse(accessToken, refreshToken, expiryTimestamp)
            )
        }
    }

    suspend fun clearCredentials() {
        dataStore.updateData { current ->
            current.copy(oAuthCreds = null)
        }
    }

    suspend fun clearToken() {
        dataStore.updateData { current ->
            current.copy(
                oAuthToken = null
            )
        }
    }

    suspend fun clearAll() {
        dataStore.updateData { OAuthConfiguration() } // Resets to default
    }
}
