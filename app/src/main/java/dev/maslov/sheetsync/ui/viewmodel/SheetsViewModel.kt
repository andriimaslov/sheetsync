package dev.maslov.sheetsync.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.DriveUiState
import dev.maslov.sheetsync.model.SheetMetadata
import dev.maslov.sheetsync.model.SheetOperationState
import dev.maslov.sheetsync.service.googleapis.DriveService
import dev.maslov.sheetsync.service.googleapis.SheetService
import dev.maslov.sheetsync.service.token.AuthorizationManager
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SheetsViewModel @Inject constructor(
    private val driveService: DriveService,
    private val authorizationManager: AuthorizationManager

) : ViewModel() {

    private val _driveUiState = MutableStateFlow<DriveUiState>(DriveUiState.Idle)
    val driveUiState: StateFlow<DriveUiState> = _driveUiState.asStateFlow()

    // In-memory cache for sheet list
    private var cachedSheets: List<SheetMetadata>? = null

    fun refreshSheetList(forceUpdate: Boolean = false) {
        viewModelScope.launch {
            _driveUiState.value = DriveUiState.Loading

            // Use cached data if available
            val cached = cachedSheets
            if (cached != null && !forceUpdate) {
                _driveUiState.value = DriveUiState.Success(cached)
                Log.d(TAG, "Using cached sheets (${cached.size} items)")
                return@launch
            }

            val accessToken = authorizationManager.validateAndRefreshToken()
            if (accessToken == null) {
                _driveUiState.value = DriveUiState.Error("Authentication required")
                return@launch
            }

            val result = driveService.getAllSheets(accessToken)
            if (result.isSuccess) {
                val sheets = result.getOrNull() ?: emptyList()
                cachedSheets = sheets
                _driveUiState.value = DriveUiState.Success(sheets)
                Log.d(TAG, "Successfully fetched ${sheets.size} sheets (cache updated)")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                _driveUiState.value = DriveUiState.Error(errorMessage)
                Log.e(TAG, "Failed to fetch sheets: $errorMessage")
            }
        }
    }

    fun resetDriveState() {
        _driveUiState.value = DriveUiState.Idle
    }

    fun clearSheetCache() {
        cachedSheets = null
        Log.d(TAG, "Sheet cache cleared")
    }

    companion object {
        private const val TAG = "SheetsViewModel"
    }
}
