package dev.maslov.sheetsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.AuthState
import dev.maslov.sheetsync.service.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState

    init {
        // Attempt to restore session on initialization
        restoreSession()
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authRepository.handleSignIn()
                .onFailure { exception ->
                    // Error is already set in authState by the repository
                }
        }
    }

    fun restoreSession() {
        viewModelScope.launch {
            authRepository.restoreUserSession()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isUserLoggedIn()
}
