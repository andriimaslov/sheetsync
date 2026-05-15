package dev.maslov.sheetsync.model.uistate

import dev.maslov.sheetsync.model.SheetMetadata

sealed class SheetListUiState {
    data object Idle : SheetListUiState()
    data object Loading : SheetListUiState()
    data class Success(val sheets: List<SheetMetadata>) : SheetListUiState()
    data class Error(val message: String) : SheetListUiState()
}
