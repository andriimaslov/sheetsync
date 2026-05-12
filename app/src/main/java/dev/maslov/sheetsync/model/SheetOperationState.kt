package dev.maslov.sheetsync.model

sealed class SheetOperationState {
    data object Idle : SheetOperationState()
    data object Loading : SheetOperationState()
    data class Success(val message: String) : SheetOperationState()
    data class Error(val message: String) : SheetOperationState()
}
