package dev.maslov.sheetsync.session
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.maslov.sheetsync.model.AuthUser
import jakarta.inject.Inject
import jakarta.inject.Named

class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("googleClientId") private val googleClientId: String
) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): AuthUser {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(googleClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = context,
            request = request
        )

        val credential = result.credential as? CustomCredential
            ?: error("Invalid credential")

        val googleCredential =
            GoogleIdTokenCredential.createFrom(credential.data)

        return AuthUser(
            userId = googleCredential.id.hashCode().toString(),
            email = googleCredential.id,
            name = googleCredential.displayName.orEmpty(),
            profilePicUrl = googleCredential.profilePictureUri?.toString()
        )
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
    }
}
