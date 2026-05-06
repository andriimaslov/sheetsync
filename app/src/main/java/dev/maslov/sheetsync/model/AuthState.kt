package dev.maslov.sheetsync.model

data class AuthState(
    val isLoggedIn: Boolean = false,
    val user: AuthUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
