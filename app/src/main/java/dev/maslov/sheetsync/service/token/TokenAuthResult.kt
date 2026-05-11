package dev.maslov.sheetsync.service.token

import android.content.IntentSender

sealed interface TokenAuthResult {
    data class AuthCode(val code: String) : TokenAuthResult
    data class NeedsResolution(val intentSender: IntentSender) : TokenAuthResult
    data object Cancelled : TokenAuthResult
    data class Error(val throwable: Throwable) : TokenAuthResult
}
