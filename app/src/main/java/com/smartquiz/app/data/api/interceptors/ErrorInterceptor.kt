package com.smartquiz.app.data.api.interceptors

import com.smartquiz.app.utils.NetworkUtils
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorInterceptor @Inject constructor(
    private val networkUtils: NetworkUtils
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Check network connectivity
        if (!networkUtils.isNetworkAvailable()) {
            throw NoNetworkException("No internet connection available")
        }
        
        return try {
            val response = chain.proceed(request)
            
            // Log response for debugging
            if (!response.isSuccessful) {
                Timber.w("API Error: ${response.code} ${response.message} for ${request.url}")
            }
            
            response
        } catch (e: Exception) {
            Timber.e(e, "Network error for ${request.url}")
            
            when (e) {
                is SocketTimeoutException -> throw TimeoutException("Request timeout", e)
                is UnknownHostException -> throw NetworkException("Unable to resolve host", e)
                is IOException -> throw NetworkException("Network error occurred", e)
                else -> throw e
            }
        }
    }
}

class NoNetworkException(message: String) : IOException(message)
class TimeoutException(message: String, cause: Throwable) : IOException(message, cause)
class NetworkException(message: String, cause: Throwable) : IOException(message, cause)