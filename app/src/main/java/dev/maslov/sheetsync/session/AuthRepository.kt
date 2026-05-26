package dev.maslov.sheetsync.session

import dev.maslov.sheetsync.model.AuthUser
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val googleAuthClient: GoogleAuthClient,
    private val localStore: AuthLocalStore
) {

    suspend fun signIn(): Result<AuthUser> = runCatching {
        val user = googleAuthClient.signIn()
        localStore.saveUser(user)
        user
    }

    suspend fun restoreSession(): AuthUser? = localStore.getSavedUserInfo()

    suspend fun signOut() {
        googleAuthClient.signOut()
        localStore.clearAll()
    }
}
