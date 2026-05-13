package dev.maslov.sheetsync.model

sealed class SheetListUiState {
    data object Idle : SheetListUiState()
    data object Loading : SheetListUiState()
    data class Success(val sheets: List<SheetMetadata>) : SheetListUiState()
    data class Error(val message: String) : SheetListUiState()
}
