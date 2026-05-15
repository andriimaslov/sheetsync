package dev.maslov.sheetsync.model.uistate

import dev.maslov.sheetsync.model.Sheet
import dev.maslov.sheetsync.model.SheetMetadata

data class TabSelectorUiState(
    val tabs: List<Sheet>,
    val isLoading: Boolean,
    val error: String?,
    val onSelect: (Sheet) -> Unit,
    val onRefresh: (SheetMetadata) -> Unit
)
