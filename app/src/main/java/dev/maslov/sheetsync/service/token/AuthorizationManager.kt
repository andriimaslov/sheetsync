package dev.maslov.sheetsync.service.token

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.gson.Gson
import dev.maslov.sheetsync.exception.TokenRefreshException
import dev.maslov.sheetsync.model.OAuthCreds
import dev.maslov.sheetsync.model.TokenResponse
import dev.maslov.sheetsync.session.AuthRequirementManager
import dev.maslov.sheetsync.session.OAuthCredManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class AuthorizationManager(
    private val context: Context,
    private val serverClientId: String,
    private val oAuthCredManager: OAuthCredManager,
    private val authRequirementManager: AuthRequirementManager
) {
    companion object {
        private const val TAG = "AuthorizationManager"
    }
    private val authorizationClient by lazy {
        Identity.getAuthorizationClient(context)
    }
    private val scopes = listOf(
        Scope("https://www.googleapis.com/auth/spreadsheets"),
        Scope("https://www.googleapis.com/auth/drive")
    )
    private val client = OkHttpClient()
    private val gson = Gson()
    val authRequiredFlow = authRequirementManager.authRequired

    private suspend fun getCredentials(): OAuthCreds = oAuthCredManager.credentialsFlow.first().oAuthCreds
        ?: throw IllegalStateException("Client credentials not found")

    suspend fun authorize(): TokenAuthResult = try {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(scopes)
            .requestOfflineAccess(serverClientId)
            .build()

        val result = authorizationClient.authorize(request).await()

        when {
            result.hasResolution() -> {
                TokenAuthResult.NeedsResolution(
                    result.pendingIntent?.intentSender
                        ?: throw IllegalStateException("Missing intent sender for resolution")
                )
            }

            !result.serverAuthCode.isNullOrBlank() -> {
                val authCode = result.serverAuthCode ?: throw IllegalStateException("Missing server auth code")
                TokenAuthResult.AuthCode(authCode)
            }

            else -> {
                TokenAuthResult.Error(
                    IllegalStateException("Missing server auth code")
                )
            }
        }
    } catch (e: Exception) {
        TokenAuthResult.Error(e)
    }

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
            val responseBody = response.body.string()

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
            val responseBody = response.body.string()

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

    suspend fun validateAndRefreshToken(): String? {
        val token = oAuthCredManager.getTokenSync()
        if (token == null) {
            Log.w(TAG, "No token available")
            authRequirementManager.requestTokenAuthentication()
            return null
        }

        if (!oAuthCredManager.isTokenExpired(token)) {
            return token.accessToken
        }

        // Token is expired, try to refresh
        val refreshToken = token.refreshToken
        if (refreshToken == null) {
            Log.w(TAG, "Token expired and no refresh token available")
            authRequirementManager.requestTokenAuthentication()
            return null
        }

        return try {
            val newTokenResponse = refreshAccessToken(refreshToken)
            oAuthCredManager.saveToken(
                accessToken = newTokenResponse.accessToken,
                refreshToken = newTokenResponse.refreshToken ?: refreshToken,
                expiryTimestamp = newTokenResponse.expiresIn
            )
            Log.d(TAG, "Token refreshed successfully")
            newTokenResponse.accessToken
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh token: ${e.message}")
            authRequirementManager.requestTokenAuthentication()
            null
        }
    }

    fun handleAuthorizationResult(data: Intent?): TokenAuthResult = try {
        val result = authorizationClient.getAuthorizationResultFromIntent(data)

        val authCode = result.serverAuthCode

        if (!authCode.isNullOrBlank()) {
            TokenAuthResult.AuthCode(authCode)
        } else {
            TokenAuthResult.Error(
                IllegalStateException("Missing auth code after resolution")
            )
        }
    } catch (e: ApiException) {
        when (e.statusCode) {
            CommonStatusCodes.CANCELED -> TokenAuthResult.Cancelled
            else -> TokenAuthResult.Error(e)
        }
    }

    suspend fun exchangeCodeForTokens(code: String) {
        try {
            val response = exchangeAuthCode(code)

            Log.d(
                ContentValues.TAG,
                "Token exchange successful"
            )

            oAuthCredManager.saveToken(response.accessToken, response.refreshToken, response.expiresIn)
            authRequirementManager.resetAuthRequirement()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Token exchange failed: ${e.message}")
        }
    }
}
