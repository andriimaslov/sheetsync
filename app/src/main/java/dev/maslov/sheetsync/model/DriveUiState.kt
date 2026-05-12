package dev.maslov.sheetsync.model

sealed class DriveUiState {
    data object Idle : DriveUiState()
    data object Loading : DriveUiState()
    data class Success(val sheets: List<SheetMetadata>) : DriveUiState()
    data class Error(val message: String) : DriveUiState()
}
