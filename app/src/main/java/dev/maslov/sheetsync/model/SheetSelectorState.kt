package dev.maslov.sheetsync.model

data class SheetSelectorState(
    val sheets: List<SheetMetadata>,
    val isLoading: Boolean,
    val error: String?,
    val onSelect: (SheetMetadata) -> Unit,
    val onRefresh: () -> Unit
)
