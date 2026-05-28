package dev.maslov.sheetsync.service.token

import android.util.Log
import com.google.auth.oauth2.ServiceAccountCredentials
import dev.maslov.sheetsync.session.OAuthCredManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ServiceAccountTokenProvider @Inject constructor(private val credentialsManager: OAuthCredManager) :
    TokenProvider {
    private var credentials: ServiceAccountCredentials? = null
    private var lastServiceAccountJson: String? = null
    private val scopes = listOf(
        "https://www.googleapis.com/auth/spreadsheets",
        "https://www.googleapis.com/auth/drive"
    )

    override suspend fun getAccessToken(): Result<String> = try {
        val config = credentialsManager.credentialsFlow.first()
        val currentJson = config.serviceAccountJson

        if (currentJson.isNullOrBlank()) {
            throw IllegalStateException("Service account JSON is missing in configuration")
        }

        if (credentials == null || currentJson != lastServiceAccountJson) {
            synchronized(this) {
                if (credentials == null || currentJson != lastServiceAccountJson) {
                    Log.d(TAG, "Creating new ServiceAccountCredentials from DataStore JSON")
                    credentials = ServiceAccountCredentials.fromStream(currentJson.byteInputStream())
                        .createScoped(scopes) as ServiceAccountCredentials
                    lastServiceAccountJson = currentJson
                }
            }
        }

        credentials?.refreshIfExpired()
        val token = credentials?.accessToken?.tokenValue
            ?: throw IllegalStateException("Service account token is null after refresh")

        Result.success(token)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get service account access token: ${e.message}", e)
        Result.failure(e)
    }

    companion object {
        private const val TAG = "ServiceAccountToken"
    }
}
