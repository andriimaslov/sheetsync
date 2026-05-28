package dev.maslov.sheetsync.session

import androidx.datastore.core.DataStore
import dev.maslov.sheetsync.model.OAuthConfiguration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class OAuthCredManager @Inject constructor(private val dataStore: DataStore<OAuthConfiguration>) {
    val credentialsFlow: Flow<OAuthConfiguration> = dataStore.data

    suspend fun saveServiceAccount(json: String, filename: String) {
        dataStore.updateData { current ->
            current.copy(
                serviceAccountJson = json,
                serviceAccountJsonName = filename
            )
        }
    }

    suspend fun clearAll() {
        dataStore.updateData { OAuthConfiguration() }
    }
}
