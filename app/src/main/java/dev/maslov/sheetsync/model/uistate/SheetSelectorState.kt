package dev.maslov.sheetsync.model.uistate

import dev.maslov.sheetsync.model.SheetMetadata

data class SheetSelectorState(
    val sheets: List<SheetMetadata>,
    val isLoading: Boolean,
    val error: String?,
    val onSelect: (SheetMetadata) -> Unit,
    val onRefresh: () -> Unit
)
