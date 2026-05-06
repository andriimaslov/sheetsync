package dev.maslov.sheetsync.model

data class AuthUser(val userId: String, val email: String, val name: String, val profilePicUrl: String? = null)
