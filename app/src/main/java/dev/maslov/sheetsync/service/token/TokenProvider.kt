package dev.maslov.sheetsync.service.token

interface TokenProvider {
    suspend fun getAccessToken(): Result<String>
}
