package dev.maslov.sheetsync.ui.viewmodel

import android.app.Application
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.uistate.ClientCredentialsUiState
import dev.maslov.sheetsync.session.OAuthCredManager
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ClientCredentialsViewModel @Inject constructor(
    private val application: Application,
    private val oAuthCredManager: OAuthCredManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ClientCredentialsUiState())
    val uiState: StateFlow<ClientCredentialsUiState> = _uiState.asStateFlow()

    init {
        observeCredentials()
    }

    fun observeCredentials() {
        viewModelScope.launch {
            oAuthCredManager.credentialsFlow.collect { config ->
                try {
                    val serviceAccountJson = config.serviceAccountJson ?: ""
                    val serviceAccountJsonName = config.serviceAccountJsonName ?: ""
                    val areSaved =
                        serviceAccountJson.isNotBlank() && serviceAccountJsonName.isNotBlank()
                    Log.d(
                        TAG,
                        "Loaded credentials"
                    )
                    _uiState.value = _uiState.value.copy(
                        serviceAccountJson = serviceAccountJson,
                        serviceAccountJsonName = serviceAccountJsonName,
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

    fun toggleShowSecret() {
        _uiState.value = _uiState.value.copy(showSecret = !_uiState.value.showSecret)
    }

    fun loadServiceAccountFile(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
                val jsonContent = withContext(Dispatchers.IO) {
                    application.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }
                }

                if (jsonContent != null) {
                    val fileName = DocumentFile.fromSingleUri(application, uri)?.name
                    _uiState.value = _uiState.value.copy(
                        serviceAccountJson = jsonContent,
                        serviceAccountJsonName = fileName ?: "unknown_file",
                        isSaving = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Could not read file content"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load service account file", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to load file: ${e.message}"
                )
            }
        }
    }

    fun saveCredentials() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

                if (_uiState.value.serviceAccountJson.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Service Account JSON must be provided"
                    )
                    return@launch
                }

                if (_uiState.value.serviceAccountJson.isNotBlank()) {
                    oAuthCredManager.saveServiceAccount(
                        _uiState.value.serviceAccountJson.trim(),
                        _uiState.value.serviceAccountJsonName
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    areSaved = true,
                    errorMessage = null
                )
                Log.d(
                    TAG,
                    "Credentials saved successfully"
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
                oAuthCredManager.clearAll()
                _uiState.value = ClientCredentialsUiState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to clear credentials: ${e.message}"
                )
            }
        }
    }
}
