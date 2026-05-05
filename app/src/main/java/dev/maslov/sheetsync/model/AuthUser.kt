package dev.maslov.sheetsync.model

data class AuthUser(val userId: String, val email: String, val name: String, val profilePicUrl: String? = null)

data class AuthState(
    val isLoggedIn: Boolean = false,
    val user: AuthUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
