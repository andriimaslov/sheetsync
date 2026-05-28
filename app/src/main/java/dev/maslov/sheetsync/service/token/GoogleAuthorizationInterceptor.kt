package dev.maslov.sheetsync.service.token

import android.util.Log
import jakarta.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class GoogleAuthorizationInterceptor @Inject constructor(private val tokenProvider: Provider<TokenProvider>) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenResult = runBlocking {
            tokenProvider.get().getAccessToken()
        }

        val requestBuilder = chain.request().newBuilder()

        tokenResult.onSuccess { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }.onFailure { exception ->
            Log.e("GoogleAuthInterceptor", "Failed to add authorization header: ${exception.message}")
        }

        return chain.proceed(requestBuilder.build())
    }
}
