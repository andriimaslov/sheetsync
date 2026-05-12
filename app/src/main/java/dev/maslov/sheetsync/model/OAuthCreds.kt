package dev.maslov.sheetsync.model

import kotlinx.serialization.Serializable

@Serializable
data class OAuthCreds(val clientId: String, val clientSecret: String)
