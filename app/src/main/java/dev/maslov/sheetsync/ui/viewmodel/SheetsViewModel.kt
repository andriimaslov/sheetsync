package dev.maslov.sheetsync.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.Sheet
import dev.maslov.sheetsync.model.SheetListUiState
import dev.maslov.sheetsync.model.SheetMetadata
import dev.maslov.sheetsync.model.Spreadsheet
import dev.maslov.sheetsync.model.TabsListUiState
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
    private val sheetService: SheetService,
    private val authorizationManager: AuthorizationManager

) : ViewModel() {

    private val _sheetListUiState = MutableStateFlow<SheetListUiState>(SheetListUiState.Idle)
    val sheetListUiState: StateFlow<SheetListUiState> = _sheetListUiState.asStateFlow()

    private val _tabListUiState = MutableStateFlow<TabsListUiState>(TabsListUiState.Idle)
    val tabListUiState: StateFlow<TabsListUiState> = _tabListUiState.asStateFlow()

    // In-memory cache for sheet list
    private var cachedSheets: List<SheetMetadata>? = null
    private var cachedTabs: List<Sheet>? = null

    fun refreshSheetList(forceUpdate: Boolean = false) {
        viewModelScope.launch {
            _sheetListUiState.value = SheetListUiState.Loading

            // Use cached data if available
            val cached = cachedSheets
            if (cached != null && !forceUpdate) {
                _sheetListUiState.value = SheetListUiState.Success(cached)
                Log.d(TAG, "Using cached sheets (${cached.size} items)")
                return@launch
            }

            val accessToken = authorizationManager.validateAndRefreshToken()
            if (accessToken == null) {
                _sheetListUiState.value = SheetListUiState.Error("Authentication required")
                return@launch
            }

            val result = driveService.getAllSheets(accessToken)
            if (result.isSuccess) {
                val sheets = result.getOrNull() ?: emptyList()
                cachedSheets = sheets
                _sheetListUiState.value = SheetListUiState.Success(sheets)
                Log.d(TAG, "Successfully fetched ${sheets.size} sheets (cache updated)")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                _sheetListUiState.value = SheetListUiState.Error(errorMessage)
                Log.e(TAG, "Failed to fetch sheets: $errorMessage")
            }
        }
    }

    fun fetchTabList(spreadsheetId: String, forceUpdate: Boolean = false) {
        viewModelScope.launch {
            _tabListUiState.value = TabsListUiState.Loading

            // Use cached data if available
            val cached = cachedTabs
            if (cached != null && !forceUpdate) {
                _tabListUiState.value = TabsListUiState.Success(cached)
                Log.d(TAG, "Using cached tabs (${cached.size} items)")
                return@launch
            }

            val accessToken = authorizationManager.validateAndRefreshToken()
            if (accessToken == null) {
                _tabListUiState.value = TabsListUiState.Error("Authentication required")
                return@launch
            }

            val result = sheetService.getSpreadsheetInfo(accessToken, spreadsheetId)
            if (result.isSuccess) {
                val spreadsheet = result.getOrNull() ?: Spreadsheet()
                _tabListUiState.value = TabsListUiState.Success(spreadsheet.sheets)
                Log.d(TAG, "Successfully fetched ${spreadsheet.sheets.size} tabs (cache updated)")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                _tabListUiState.value = TabsListUiState.Error(errorMessage)
                Log.e(TAG, "Failed to fetch tabs: $errorMessage")
            }
        }
    }

    fun resetDriveState() {
        _sheetListUiState.value = SheetListUiState.Idle
    }

    fun clearSheetCache() {
        cachedSheets = null
        Log.d(TAG, "Sheet cache cleared")
    }

    companion object {
        private const val TAG = "SheetsViewModel"
    }
}
