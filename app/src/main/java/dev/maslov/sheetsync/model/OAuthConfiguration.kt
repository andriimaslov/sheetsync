package dev.maslov.sheetsync.model

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OAuthConfiguration(val oAuthCreds: OAuthCreds? = null, val oAuthToken: OAuthToken? = null)

object AppCredentialsSerializer : Serializer<OAuthConfiguration> {
    override val defaultValue = OAuthConfiguration()

    override suspend fun readFrom(input: InputStream): OAuthConfiguration = try {
        Json.decodeFromString(
            OAuthConfiguration.serializer(),
            input.readBytes().decodeToString()
        )
    } catch (e: Exception) {
        defaultValue
    }

    override suspend fun writeTo(t: OAuthConfiguration, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(Json.encodeToString(OAuthConfiguration.serializer(), t).toByteArray())
        }
    }
}
