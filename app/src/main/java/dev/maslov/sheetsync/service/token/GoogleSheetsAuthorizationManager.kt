package dev.maslov.sheetsync.service.token

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import kotlin.getValue
import kotlinx.coroutines.tasks.await

class GoogleSheetsAuthorizationManager(private val context: Context, private val serverClientId: String) {

    private val authorizationClient by lazy {
        Identity.getAuthorizationClient(context)
    }

    private val scopes = listOf(
        Scope("https://www.googleapis.com/auth/spreadsheets"),
        Scope("https://www.googleapis.com/auth/drive")
    )

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
}
