package dev.maslov.sheetsync.configuration

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.maslov.sheetsync.model.AppCredentialsSerializer
import dev.maslov.sheetsync.model.OAuthConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecureDataStore(@ApplicationContext context: Context): DataStore<OAuthConfiguration> {
        AeadConfig.register()

        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "sheetsync_keyset", "master_key_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://sheetsync_key_v2")
            .build()
            .keysetHandle

        val aead = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        val secureSerializer = AeadSerializer(
            aead = aead,
            wrappedSerializer = AppCredentialsSerializer,
            // Custom context-string to prevent "Key Substitution" attacks
            associatedData = "auth_context_v1".encodeToByteArray()
        )

        return DataStoreFactory.create(
            serializer = secureSerializer,
            produceFile = { context.dataStoreFile("secure_creds.json") }
        )
    }
}
