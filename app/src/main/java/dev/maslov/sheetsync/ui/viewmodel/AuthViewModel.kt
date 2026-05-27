package dev.maslov.sheetsync.ui.viewmodel

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.AuthState
import dev.maslov.sheetsync.service.token.AuthorizationManager
import dev.maslov.sheetsync.service.token.TokenAuthResult
import dev.maslov.sheetsync.session.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authorizationManager: AuthorizationManager
) : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    private val _resolutionTrigger = Channel<IntentSender>(Channel.BUFFERED)
    val resolutionTrigger = _resolutionTrigger.receiveAsFlow()

    init {
        restoreSession()
        observeAuthRequirement()
    }

    private fun observeAuthRequirement() {
        viewModelScope.launch {
            authorizationManager.authRequiredFlow.collect { isRequired ->
                if (isRequired) {
                    Log.d(TAG, "Auth requirement detected, initiating authorization")
                    beginSheetsAuthorization()
                }
            }
        }
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

    fun beginSheetsAuthorization() {
        viewModelScope.launch {
            when (val result = authorizationManager.authorize()) {
                is TokenAuthResult.AuthCode -> {
                    Log.d(TAG, "Authorization successful, got auth code: ${result.code}")
                    authorizationManager.exchangeCodeForTokens(result.code)
                }
                is TokenAuthResult.NeedsResolution -> {
                    Log.d(TAG, "Authorization requires resolution, sending intent sender to UI")
                    _authState.update { it.copy(isLoading = false) }
                    _resolutionTrigger.send(result.intentSender)
                }
                is TokenAuthResult.Error -> {
                    Log.d(TAG, "Authorization failed: ${result.throwable.message}")
                    _authState.update { it.copy(isLoading = false, error = result.throwable.message) }
                    authorizationManager.resetAuthRequirement()
                }
                TokenAuthResult.Cancelled -> {
                    Log.d(TAG, "Authorization cancelled")
                    _authState.update { it.copy(isLoading = false) }
                    authorizationManager.resetAuthRequirement()
                }
            }
        }
    }

    fun onSheetsResolutionResult(data: Intent?) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }

            when (val result = authorizationManager.handleAuthorizationResult(data)) {
                is TokenAuthResult.AuthCode -> {
                    Log.d(TAG, "Authorization resolution successful, got auth code: ${result.code}")
                    authorizationManager.exchangeCodeForTokens(result.code)
                }

                is TokenAuthResult.Error -> {
                    Log.d(TAG, "Authorization resolution failed: ${result.throwable.message}")
                    _authState.update { it.copy(isLoading = false, error = result.throwable.message) }
                    authorizationManager.resetAuthRequirement()
                }

                else -> {
                    Log.d(TAG, "Authorization resolution cancelled or unexpected result")
                    _authState.update { it.copy(isLoading = false) }
                    authorizationManager.resetAuthRequirement()
                }
            }
        }
    }

    fun clearToken() {
        viewModelScope.launch {
            authorizationManager.clearToken()
        }
    }
}
