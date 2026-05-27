package dev.maslov.sheetsync.ui.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.OAuthCreds
import dev.maslov.sheetsync.model.uistate.ClientCredentialsUiState
import dev.maslov.sheetsync.session.OAuthCredManager
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ClientCredentialsViewModel @Inject constructor(private val oAuthCredManager: OAuthCredManager) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientCredentialsUiState())
    val uiState: StateFlow<ClientCredentialsUiState> = _uiState.asStateFlow()

    val credentials: StateFlow<OAuthCreds?> = oAuthCredManager.credentialsFlow
        .map { it.oAuthCreds }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        observeCredentials()
    }

    fun observeCredentials() {
        viewModelScope.launch {
            credentials.collect { creds ->
                try {
                    val areSaved = creds != null && creds.clientId.isNotBlank() && creds.clientSecret.isNotBlank()
                    Log.d(
                        TAG,
                        "Loaded credentials"
                    )
                    _uiState.value = _uiState.value.copy(
                        clientId = credentials.value?.clientId ?: "",
                        clientSecret = credentials.value?.clientSecret ?: "",
                        areSaved = areSaved,
                        errorMessage = null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to load credentials: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateClientId(value: String) {
        _uiState.value = _uiState.value.copy(clientId = value)
    }

    fun updateClientSecret(value: String) {
        _uiState.value = _uiState.value.copy(clientSecret = value)
    }

    fun toggleShowSecret() {
        _uiState.value = _uiState.value.copy(showSecret = !_uiState.value.showSecret)
    }

    fun saveCredentials() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

                val clientId = _uiState.value.clientId.trim()
                val clientSecret = _uiState.value.clientSecret.trim()

                if (clientId.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Client ID cannot be empty"
                    )
                    return@launch
                }

                if (clientSecret.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Client Secret cannot be empty"
                    )
                    return@launch
                }

                oAuthCredManager.saveCredentials(clientId, clientSecret)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    areSaved = true,
                    errorMessage = null
                )
                Log.d(
                    TAG,
                    "Credentials saved successfully: clientId=${clientId.take(
                        10
                    )}..., clientSecret=${clientSecret.take(10)}..."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save credentials: ${e.message}"
                )
            }
        }
    }

    fun clearCredentials() {
        viewModelScope.launch {
            try {
                oAuthCredManager.clearCredentials()
                _uiState.value = ClientCredentialsUiState(showSecret = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to clear credentials: ${e.message}"
                )
            }
        }
    }
}
