package dev.maslov.sheetsync.ui.viewmodel

import android.util.Log
import androidx.collection.SieveCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.Sheet
import dev.maslov.sheetsync.model.SheetMetadata
import dev.maslov.sheetsync.model.Spreadsheet
import dev.maslov.sheetsync.model.uistate.SheetListUiState
import dev.maslov.sheetsync.model.uistate.TabsListUiState
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

    private val sheetCache = SieveCache<String, CachedValue<List<SheetMetadata>>>(1)
    private val tabCache = SieveCache<String, CachedValue<List<Sheet>>>(50)

    private data class CachedValue<T>(val value: T, val timestamp: Long = System.currentTimeMillis())

    fun refreshSheetList(forceUpdate: Boolean = false) {
        viewModelScope.launch {
            _sheetListUiState.value = SheetListUiState.Loading
            Log.d(TAG, "Refresh sheet list force = $forceUpdate")
            // Use cached data if available and not expired
            val cached = sheetCache[KEY_SHEETS]
            if (cached != null && !forceUpdate && (System.currentTimeMillis() - cached.timestamp < SHEETS_TTL)) {
                _sheetListUiState.value = SheetListUiState.Success(cached.value)
                Log.d(TAG, "Using cached sheets (${cached.value.size} items)")
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
                sheetCache.put(KEY_SHEETS, CachedValue(sheets))
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
            Log.d(TAG, "Refresh tab list force = $forceUpdate")
            // Use cached data if available and not expired
            val cached = tabCache[spreadsheetId]
            if (cached != null && !forceUpdate && (System.currentTimeMillis() - cached.timestamp < TABS_TTL)) {
                // Return Success with the cached tabs for the requested ID
                _tabListUiState.value = TabsListUiState.Success(cached.value)
                Log.d(TAG, "Using cached tabs for $spreadsheetId")
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
                tabCache.put(spreadsheetId, CachedValue(spreadsheet.sheets))
                _tabListUiState.value = TabsListUiState.Success(spreadsheet.sheets)
                Log.d(TAG, "Successfully fetched ${spreadsheet.sheets.size} tabs for $spreadsheetId (cache updated)")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                _tabListUiState.value = TabsListUiState.Error(errorMessage)
                Log.e(TAG, "Failed to fetch tabs: $errorMessage")
            }
        }
    }

    companion object {
        private const val TAG = "SheetsViewModel"
        private const val KEY_SHEETS = "all_sheets"

        // TTL constants
        private const val SHEETS_TTL = 5 * 60 * 1000L // 5 minutes
        private const val TABS_TTL = 10 * 60 * 1000L // 10 minutes
    }
}
