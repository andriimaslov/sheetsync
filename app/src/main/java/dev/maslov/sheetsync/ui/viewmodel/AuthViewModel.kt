package dev.maslov.sheetsync.ui.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
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
        restoreSession()
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authRepository.handleSignIn()
                .onFailure { exception ->
                    Log.d(TAG, "Google Sign-In failed: ${exception.message}")
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
