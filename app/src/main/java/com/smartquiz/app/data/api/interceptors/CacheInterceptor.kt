package com.smartquiz.app.data.api.interceptors

import com.smartquiz.app.utils.NetworkUtils
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheInterceptor @Inject constructor(
    private val networkUtils: NetworkUtils
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalResponse = chain.proceed(request)
        
        val cacheControl = when {
            // Cache static content for longer
            request.url.pathSegments.any { it in listOf("subjects", "topics") } -> {
                CacheControl.Builder()
                    .maxAge(1, TimeUnit.HOURS)
                    .build()
            }
            
            // Cache questions for shorter time
            request.url.pathSegments.contains("questions") -> {
                CacheControl.Builder()
                    .maxAge(15, TimeUnit.MINUTES)
                    .build()
            }
            
            // Don't cache user-specific or dynamic content
            request.url.pathSegments.any { it in listOf("profile", "sync", "analytics") } -> {
                CacheControl.Builder()
                    .noCache()
                    .build()
            }
            
            else -> {
                CacheControl.Builder()
                    .maxAge(5, TimeUnit.MINUTES)
                    .build()
            }
        }
        
        return originalResponse.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}