package dev.maslov.sheetsync.model.uistate

data class ClientCredentialsUiState(
    val clientId: String = "",
    val clientSecret: String = "",
    val showSecret: Boolean = false,
    val areSaved: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
