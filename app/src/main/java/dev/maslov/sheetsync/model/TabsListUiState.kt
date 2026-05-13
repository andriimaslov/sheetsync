package dev.maslov.sheetsync.model

sealed class TabsListUiState {
    data object Idle : TabsListUiState()
    data object Loading : TabsListUiState()
    data class Success(val tabs: List<Sheet>) : TabsListUiState()
    data class Error(val message: String) : TabsListUiState()
}
