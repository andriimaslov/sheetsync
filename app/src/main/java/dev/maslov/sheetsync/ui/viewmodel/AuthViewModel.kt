package dev.maslov.sheetsync.ui.viewmodel

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.AuthState
import dev.maslov.sheetsync.service.token.GoogleSheetsAuthorizationManager
import dev.maslov.sheetsync.service.token.GoogleTokenExchangeService
import dev.maslov.sheetsync.service.token.TokenAuthResult
import dev.maslov.sheetsync.service.token.TokenRepository
import dev.maslov.sheetsync.session.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sheetsManager: GoogleSheetsAuthorizationManager,
    private val tokenService: GoogleTokenExchangeService,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    // A "Side Effect" flow to trigger the Google Sheets popup in the Activity
    private val _resolutionTrigger = Channel<IntentSender>(Channel.BUFFERED)
    val resolutionTrigger = _resolutionTrigger.receiveAsFlow()

    private val token = tokenRepository.tokenFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        restoreSession()
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            authRepository.signIn()
                .onSuccess { user ->
                    _authState.value = AuthState(
                        isLoggedIn = true,
                        user = user
                    )
                }
                .onFailure { e ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
        }
    }

    fun restoreSession() {
        viewModelScope.launch {
            val user = authRepository.restoreSession()
            _authState.value =
                if (user != null) {
                    AuthState(isLoggedIn = true, user = user)
                } else {
                    AuthState()
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState()
        }
    }

    fun isLoggedIn(): Boolean = _authState.value.isLoggedIn

    fun beginSheetsAuthorization() {
        viewModelScope.launch {
            when (val result = sheetsManager.authorize()) {
                is TokenAuthResult.AuthCode -> {
                    // If the user already granted permission, go straight to exchange
                    Log.d(TAG, "Authorization successful, got auth code: ${result.code}")
                    exchangeCodeForTokens(result.code)
                }
                is TokenAuthResult.NeedsResolution -> {
                    // If we need a popup, stop loading and signal the UI
                    Log.d(TAG, "Authorization requires resolution, sending intent sender to UI")
                    _authState.update { it.copy(isLoading = false) }
                    _resolutionTrigger.send(result.intentSender)
                }
                is TokenAuthResult.Error -> {
                    Log.d(TAG, "Authorization failed: ${result.throwable.message}")
                    _authState.update { it.copy(isLoading = false, error = result.throwable.message) }
                }
                TokenAuthResult.Cancelled -> {
                    Log.d(TAG, "Authorization cancelled")
                    _authState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // Called by the Activity/Fragment after the resolution popup finishes
    fun onSheetsResolutionResult(data: Intent?) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }

            when (val result = sheetsManager.handleAuthorizationResult(data)) {
                is TokenAuthResult.AuthCode -> {
                    Log.d(TAG, "Authorization resolution successful, got auth code: ${result.code}")
                    exchangeCodeForTokens(result.code)
                }

                is TokenAuthResult.Error -> {
                    Log.d(TAG, "Authorization resolution failed: ${result.throwable.message}")
                    _authState.update { it.copy(isLoading = false, error = result.throwable.message) }
                }

                else -> {
                    Log.d(TAG, "Authorization resolution cancelled or unexpected result")
                    _authState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private suspend fun exchangeCodeForTokens(code: String) {
        try {
            val response = tokenService.exchangeAuthCode(code)

            Log.d(
                TAG,
                "Token exchange successful: accessToken=${response.accessToken}, refreshToken=${response.refreshToken}"
            )

            tokenRepository.saveToken(response.accessToken, response.refreshToken)
            _authState.update { it.copy(isLoading = false, error = null, isGoogleAPIAuthorized = true) }
        } catch (e: Exception) {
            _authState.update { it.copy(isLoading = false, error = "Token exchange failed: ${e.message}", isGoogleAPIAuthorized = false) }
        }
    }
}
