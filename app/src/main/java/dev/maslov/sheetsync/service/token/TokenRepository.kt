package dev.maslov.sheetsync.service.token

import dev.maslov.sheetsync.model.Token

class TokenRepository(private val dao: TokenDao) {
    val tokenFlow = dao.getToken()

    suspend fun saveToken(accessToken: String, refreshToken: String?) {
        val token = Token(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtMillis = 3600L
        )
        dao.saveToken(token)
    }

    suspend fun cleanToken() {
        dao.cleanToken()
    }
}
