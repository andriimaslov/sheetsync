package dev.maslov.sheetsync.model

import android.util.Log
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OAuthConfiguration(val serviceAccountJson: String? = null, val serviceAccountJsonName: String? = null)

object AppCredentialsSerializer : Serializer<OAuthConfiguration> {
    private const val TAG = "AppCredentialsSerializer"
    override val defaultValue = OAuthConfiguration()

    override suspend fun readFrom(input: InputStream): OAuthConfiguration = try {
        Json.decodeFromString(
            OAuthConfiguration.serializer(),
            input.readBytes().decodeToString()
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error happen during deserialization. ${e.message}")
        defaultValue
    }

    override suspend fun writeTo(t: OAuthConfiguration, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(Json.encodeToString(OAuthConfiguration.serializer(), t).toByteArray())
        }
    }
}
