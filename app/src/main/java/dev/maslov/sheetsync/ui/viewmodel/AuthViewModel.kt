package dev.maslov.sheetsync.ui.viewmodel

import android.content.Intent
import android.content.IntentSender
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

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
}
