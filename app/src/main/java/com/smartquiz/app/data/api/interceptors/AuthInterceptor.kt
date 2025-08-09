package com.smartquiz.app.data.api.interceptors

import com.smartquiz.app.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for certain endpoints
        val skipAuth = originalRequest.url.pathSegments.any { segment ->
            segment in listOf("login", "register", "health", "subjects")
        }
        
        if (skipAuth) {
            return chain.proceed(originalRequest)
        }
        
        // Add auth token if available
        val token = runBlocking {
            try {
                // You would get this from your auth token storage
                // For now, we'll skip adding the token
                null
            } catch (e: Exception) {
                null
            }
        }
        
        val requestBuilder = originalRequest.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "SmartQuiz-Android/1.0")
        
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}