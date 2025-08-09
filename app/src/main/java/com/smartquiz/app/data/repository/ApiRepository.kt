package com.smartquiz.app.data.repository

import com.smartquiz.app.data.api.ApiService
import com.smartquiz.app.data.api.interceptors.NetworkException
import com.smartquiz.app.data.api.interceptors.NoNetworkException
import com.smartquiz.app.data.api.interceptors.TimeoutException
import com.smartquiz.app.data.models.*
import com.smartquiz.app.utils.NetworkUtils
import com.smartquiz.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor(
    private val apiService: ApiService,
    private val networkUtils: NetworkUtils
) {

    suspend fun generateQuestions(request: GenerateQuestionsRequest): Resource<GenerateQuestionsResponse> {
        return safeApiCall {
            apiService.generateQuestions(request)
        }
    }

    suspend fun getQuestions(
        subject: String,
        difficulty: String,
        limit: Int = 10,
        offset: Int = 0
    ): Resource<QuestionsResponse> {
        return safeApiCall {
            apiService.getQuestions(subject, difficulty, limit, offset)
        }
    }

    suspend fun generateFeedback(request: GenerateFeedbackRequest): Resource<GenerateFeedbackResponse> {
        return safeApiCall {
            apiService.generateFeedback(request)
        }
    }

    suspend fun registerUser(request: RegisterUserRequest): Resource<UserResponse> {
        return safeApiCall {
            apiService.registerUser(request)
        }
    }

    suspend fun loginUser(request: LoginRequest): Resource<LoginResponse> {
        return safeApiCall {
            apiService.loginUser(request)
        }
    }

    suspend fun getUserProfile(token: String): Resource<UserProfileResponse> {
        return safeApiCall {
            apiService.getUserProfile(token)
        }
    }

    suspend fun syncQuizzes(token: String, request: SyncQuizzesRequest): Resource<SyncQuizzesResponse> {
        return safeApiCall {
            apiService.syncQuizzes(token, request)
        }
    }

    suspend fun getLeaderboard(
        subject: String? = null,
        timeframe: String = "week",
        limit: Int = 50
    ): Resource<LeaderboardResponse> {
        return safeApiCall {
            apiService.getLeaderboard(subject, timeframe, limit)
        }
    }

    suspend fun reportQuizCompleted(token: String, request: QuizCompletedRequest): Resource<AnalyticsResponse> {
        return safeApiCall {
            apiService.reportQuizCompleted(token, request)
        }
    }

    suspend fun getUserStats(token: String, timeframe: String = "month"): Resource<UserStatsResponse> {
        return safeApiCall {
            apiService.getUserStats(token, timeframe)
        }
    }

    suspend fun getSubjects(): Resource<SubjectsResponse> {
        return safeApiCall {
            apiService.getSubjects()
        }
    }

    suspend fun getTopicsBySubject(subject: String): Resource<TopicsResponse> {
        return safeApiCall {
            apiService.getTopicsBySubject(subject)
        }
    }

    suspend fun healthCheck(): Resource<HealthResponse> {
        return safeApiCall {
            apiService.healthCheck()
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                if (!networkUtils.isNetworkAvailable()) {
                    return@withContext Resource.Error("No internet connection available")
                }

                val response = apiCall()
                
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        Resource.Success(body)
                    } ?: Resource.Error("Empty response body")
                } else {
                    val errorMessage = parseErrorMessage(response)
                    Timber.e("API Error: ${response.code()} - $errorMessage")
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Timber.e(e, "API call failed")
                val errorMessage = when (e) {
                    is NoNetworkException -> "No internet connection"
                    is TimeoutException -> "Request timeout. Please try again."
                    is NetworkException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred: ${e.message}"
                }
                Resource.Error(errorMessage)
            }
        }
    }

    private fun <T> parseErrorMessage(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                // Try to parse error body as JSON
                val gson = com.google.gson.Gson()
                val apiError = gson.fromJson(errorBody, ApiError::class.java)
                apiError.message
            } else {
                getDefaultErrorMessage(response.code())
            }
        } catch (e: Exception) {
            getDefaultErrorMessage(response.code())
        }
    }

    private fun getDefaultErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Bad request. Please check your input."
            401 -> "Authentication failed. Please login again."
            403 -> "Access denied. You don't have permission."
            404 -> "Resource not found."
            408 -> "Request timeout. Please try again."
            429 -> "Too many requests. Please wait and try again."
            500 -> "Server error. Please try again later."
            502, 503 -> "Service temporarily unavailable."
            else -> "Error occurred (Code: $code)"
        }
    }
}