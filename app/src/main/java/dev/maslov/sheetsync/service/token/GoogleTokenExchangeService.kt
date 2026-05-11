package dev.maslov.sheetsync.service.token

import com.google.gson.Gson
import dev.maslov.sheetsync.exception.TokenRefreshException
import dev.maslov.sheetsync.model.ClientCredentials
import dev.maslov.sheetsync.model.TokenResponse
import dev.maslov.sheetsync.service.credentials.ClientCredentialsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class GoogleTokenExchangeService(private val clientCredentialRepository: ClientCredentialsRepository) {

    private val client = OkHttpClient()
    private val gson = Gson()

    private suspend fun getCredentials(): ClientCredentials = clientCredentialRepository.credentialsFlow.first()
        ?: throw IllegalStateException("No credentials found in repository")

    suspend fun exchangeAuthCode(code: String): TokenResponse = withContext(Dispatchers.IO) {
        val credentials = getCredentials()
        val body = FormBody.Builder()
            .add("code", code)
            .add("client_id", credentials.clientId)
            .add("client_secret", credentials.clientSecret)
            .add("grant_type", "authorization_code")
            .add("redirect_uri", "")
            .build()

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
                ?: throw IllegalStateException("Empty response body")

            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Token exchange failed: HTTP ${response.code}, body=$responseBody"
                )
            }

            try {
                gson.fromJson(responseBody, TokenResponse::class.java)
            } catch (e: Exception) {
                throw IllegalStateException("Invalid token response: ${e.message}, body=$responseBody}")
            }
        }
    }

    suspend fun refreshAccessToken(refreshToken: String): TokenResponse = withContext(Dispatchers.IO) {
        val credentials = getCredentials()
        val body = FormBody.Builder()
            .add("client_id", credentials.clientId)
            .add("client_secret", credentials.clientSecret)
            .add("refresh_token", refreshToken)
            .add("grant_type", "refresh_token")
            .build()

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
                ?: throw IllegalStateException("Empty response body")

            if (!response.isSuccessful) {
                throw TokenRefreshException(
                    "Refresh failed: HTTP ${response.code}, body=$responseBody"
                )
            }

            try {
                gson.fromJson(responseBody, TokenResponse::class.java)
            } catch (e: Exception) {
                throw IllegalStateException("Invalid token response: ${e.message}, body=$responseBody}")
            }
        }
    }
}
