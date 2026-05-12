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
import dev.maslov.sheetsync.service.token.GoogleTokenExchangeService
import dev.maslov.sheetsync.session.AuthRequirementManager
import dev.maslov.sheetsync.session.OAuthCredManager
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SheetsViewModel @Inject constructor(
    private val driveService: DriveService,
    private val sheetService: SheetService,
    private val tokenExchangeService: GoogleTokenExchangeService,
    private val oAuthCredManager: OAuthCredManager,
    private val authRequirementManager: AuthRequirementManager
) : ViewModel() {

    private val _driveUiState = MutableStateFlow<DriveUiState>(DriveUiState.Idle)
    val driveUiState: StateFlow<DriveUiState> = _driveUiState.asStateFlow()

    private val _sheetOperationState = MutableStateFlow<SheetOperationState>(SheetOperationState.Idle)
    val sheetOperationState: StateFlow<SheetOperationState> = _sheetOperationState.asStateFlow()

    // In-memory cache for sheet list
    private var cachedSheets: List<SheetMetadata>? = null

    suspend fun validateAndRefreshToken(): String? {
        val token = oAuthCredManager.getTokenSync()
        if (token == null) {
            Log.w(TAG, "No token available")
            authRequirementManager.requestTokenAuthentication()
            return null
        }

        if (!oAuthCredManager.isTokenExpired(token)) {
            return token.accessToken
        }

        // Token is expired, try to refresh
        val refreshToken = token.refreshToken
        if (refreshToken == null) {
            Log.w(TAG, "Token expired and no refresh token available")
            authRequirementManager.requestTokenAuthentication()
            return null
        }

        return try {
            val newTokenResponse = tokenExchangeService.refreshAccessToken(refreshToken)
            oAuthCredManager.saveToken(
                accessToken = newTokenResponse.accessToken,
                refreshToken = newTokenResponse.refreshToken ?: refreshToken,
                expiryTimestamp = newTokenResponse.expiresIn
            )
            Log.d(TAG, "Token refreshed successfully")
            newTokenResponse.accessToken
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh token: ${e.message}")
            authRequirementManager.requestTokenAuthentication()
            null
        }
    }

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

            val accessToken = validateAndRefreshToken()
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

//    fun fetchAllSheets(accessToken: String) {
//        viewModelScope.launch {
//
//
//            // No cache, fetch fresh data
//            _driveUiState.value = DriveUiState.Loading
//            val result = driveService.getAllSheets(accessToken)
//            if (result.isSuccess) {
//                val sheets = result.getOrNull() ?: emptyList()
//                cachedSheets = sheets
//                _driveUiState.value = DriveUiState.Success(sheets)
//                Log.d(TAG, "Successfully fetched ${sheets.size} sheets (cache populated)")
//            } else {
//                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error occurred"
//                _driveUiState.value = DriveUiState.Error(errorMessage)
//                Log.e(TAG, "Failed to fetch sheets: $errorMessage")
//            }
//        }
//    }

    fun appendToSheet(accessToken: String, spreadsheetId: String, values: List<String>, range: String = "Sheet1!A1") {
        viewModelScope.launch {
            _sheetOperationState.value = SheetOperationState.Loading
            val result = sheetService.appendToSheet(
                accessToken = accessToken,
                spreadsheetId = spreadsheetId,
                range = range,
                values = values
            )
            if (result.isSuccess) {
                val successMessage = "Appended ${values.size} values to sheet"
                _sheetOperationState.value = SheetOperationState.Success(successMessage)
                Log.d(TAG, successMessage)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                _sheetOperationState.value = SheetOperationState.Error(errorMessage)
                Log.e(TAG, "Failed to append to sheet: $errorMessage")
            }
        }
    }

    fun appendRowToSheet(accessToken: String, spreadsheetId: String, row: List<Any>, range: String = "Sheet1!A1") {
        viewModelScope.launch {
            _sheetOperationState.value = SheetOperationState.Loading
            val result = sheetService.appendToSheetAsRow(
                accessToken = accessToken,
                spreadsheetId = spreadsheetId,
                range = range,
                values = row
            )
            if (result.isSuccess) {
                val successMessage = "Appended row with ${row.size} values to sheet"
                _sheetOperationState.value = SheetOperationState.Success(successMessage)
                Log.d(TAG, successMessage)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                _sheetOperationState.value = SheetOperationState.Error(errorMessage)
                Log.e(TAG, "Failed to append row to sheet: $errorMessage")
            }
        }
    }

    fun resetOperationState() {
        _sheetOperationState.value = SheetOperationState.Idle
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
