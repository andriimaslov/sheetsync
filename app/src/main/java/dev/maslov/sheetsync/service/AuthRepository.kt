package dev.maslov.sheetsync.service

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.maslov.sheetsync.model.AuthState
import dev.maslov.sheetsync.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(private val context: Context, private val googleClientId: String) {
    private val _authState = MutableStateFlow<AuthState>(AuthState())
    val authState = _authState.asStateFlow()

    private val credentialManager = CredentialManager.create(context)

//    init {
//        // Restore user session on initialization
//        restoreUserSession()
//    }

    suspend fun restoreUserSession() {
        try {
            AuthPreferences.getSavedUserInfo(context).collect { user ->
                if (user != null) {
                    val user = AuthUser(
                        userId = user.userId,
                        email = user.email,
                        name = user.name,
                        profilePicUrl = user.profilePicUrl
                    )
                    _authState.value = AuthState(isLoggedIn = true, user = user)
                    Log.d(TAG, "User session restored: ${user.email}")
                } else {
                    Log.d(TAG, "No saved user session found")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring user session: ${e.message}")
        }
    }

    suspend fun handleSignIn(): Result<Unit> = runCatching {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(googleClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credential = credentialManager.getCredential(
            context = context,
            request = request
        ).credential

        handleCredentialResult(credential)
    }

    private suspend fun handleCredentialResult(credential: Credential) {
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val email = googleIdTokenCredential.id
                        val displayName = googleIdTokenCredential.displayName ?: ""
                        val profilePicUrl = googleIdTokenCredential.profilePictureUri?.toString()

                        val user = AuthUser(
                            userId = email.hashCode().toString(),
                            email = email,
                            name = displayName,
                            profilePicUrl = profilePicUrl
                        )

                        // Save user info for session restoration
                        AuthPreferences.saveUserInfo(
                            context = context,
                            userId = user.userId,
                            email = user.email,
                            name = user.name,
                            profilePicUrl = user.profilePicUrl
                        )

                        _authState.value = AuthState(
                            isLoggedIn = true,
                            user = user,
                            error = null
                        )
                        Log.d(TAG, "User signed in successfully: $email (idToken: ${idToken.take(20)}...)")
                    } catch (e: Exception) {
                        val errorMessage = "Failed to parse Google credential: ${e.message}"
                        _authState.value = _authState.value.copy(error = errorMessage)
                        Log.e(TAG, errorMessage)
                        throw e
                    }
                } else {
                    val errorMessage = "Unexpected credential type: ${credential.type}"
                    _authState.value = _authState.value.copy(error = errorMessage)
                    Log.e(TAG, errorMessage)
                    throw IllegalArgumentException(errorMessage)
                }
            }
            else -> {
                val errorMessage = "Unexpected credential type during sign-in"
                _authState.value = _authState.value.copy(error = errorMessage)
                Log.e(TAG, errorMessage)
                throw IllegalArgumentException(errorMessage)
            }
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
            AuthPreferences.clearAll(context)
            _authState.value = AuthState()
            Log.d(TAG, "User signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.message}")
        }
    }

    fun isUserLoggedIn(): Boolean = _authState.value.isLoggedIn

    companion object {
        private const val TAG = "AuthRepository"
    }
}
