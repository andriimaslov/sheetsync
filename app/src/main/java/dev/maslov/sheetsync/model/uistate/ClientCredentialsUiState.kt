package dev.maslov.sheetsync.model.uistate

data class ClientCredentialsUiState(
    val serviceAccountJson: String = "",
    val serviceAccountJsonName: String = "",
    val showSecret: Boolean = false,
    val areSaved: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
