package dev.maslov.sheetsync.service.credentials

import androidx.compose.runtime.collectAsState
import dev.maslov.sheetsync.model.ClientCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClientCredentialsRepository(private val dao: ClientCredentialsDao) {
    val credentialsFlow = dao.getCredentials()

    suspend fun saveCredentials(clientId: String, clientSecret: String) {
        val credentials = ClientCredentials(
            clientId = clientId,
            clientSecret = clientSecret
        )
        dao.saveCredentials(credentials)
    }

    suspend fun clearCredentials() {
        dao.clearCredentials()
    }

    companion object {
        /**
         * Simple encryption utility for sensitive strings
         */
        fun encryptString(value: String): String = try {
            android.util.Base64.encodeToString(
                value.toByteArray(Charsets.UTF_8),
                android.util.Base64.DEFAULT
            )
        } catch (e: Exception) {
            value
        }

        /**
         * Simple decryption utility for sensitive strings
         */
        fun decryptString(encryptedValue: String): String = try {
            String(
                android.util.Base64.decode(encryptedValue, android.util.Base64.DEFAULT),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            encryptedValue
        }
    }
}
