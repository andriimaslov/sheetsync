package dev.maslov.sheetsync.model

import kotlinx.serialization.Serializable

@Serializable
data class OAuthToken(val accessToken: String, val refreshToken: String? = null, val expiresAtMillis: Long) {
    companion object {
        fun fromResponse(accessToken: String, refreshToken: String?, expiresInSeconds: Long): OAuthToken = OAuthToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtMillis = System.currentTimeMillis() + expiresInSeconds * 1000
        )
    }
}
