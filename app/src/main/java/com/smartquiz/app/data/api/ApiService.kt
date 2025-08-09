package com.smartquiz.app.data.api

import com.smartquiz.app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Question Generation
    @POST("api/v1/questions/generate")
    suspend fun generateQuestions(
        @Body request: GenerateQuestionsRequest
    ): Response<GenerateQuestionsResponse>

    @GET("api/v1/questions")
    suspend fun getQuestions(
        @Query("subject") subject: String,
        @Query("difficulty") difficulty: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<QuestionsResponse>

    @GET("api/v1/questions/{id}")
    suspend fun getQuestionById(
        @Path("id") questionId: String
    ): Response<QuestionResponse>

    // Feedback Generation
    @POST("api/v1/feedback/generate")
    suspend fun generateFeedback(
        @Body request: GenerateFeedbackRequest
    ): Response<GenerateFeedbackResponse>

    // User Management
    @POST("api/v1/users/register")
    suspend fun registerUser(
        @Body request: RegisterUserRequest
    ): Response<UserResponse>

    @POST("api/v1/users/login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/v1/users/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    @PUT("api/v1/users/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateUserProfileRequest
    ): Response<UserResponse>

    // Quiz Sync
    @POST("api/v1/quizzes/sync")
    suspend fun syncQuizzes(
        @Header("Authorization") token: String,
        @Body request: SyncQuizzesRequest
    ): Response<SyncQuizzesResponse>

    @GET("api/v1/quizzes/leaderboard")
    suspend fun getLeaderboard(
        @Query("subject") subject: String? = null,
        @Query("timeframe") timeframe: String = "week", // week, month, all
        @Query("limit") limit: Int = 50
    ): Response<LeaderboardResponse>

    // Analytics
    @POST("api/v1/analytics/quiz-completed")
    suspend fun reportQuizCompleted(
        @Header("Authorization") token: String,
        @Body request: QuizCompletedRequest
    ): Response<AnalyticsResponse>

    @GET("api/v1/analytics/user-stats")
    suspend fun getUserStats(
        @Header("Authorization") token: String,
        @Query("timeframe") timeframe: String = "month"
    ): Response<UserStatsResponse>

    // Content Management
    @GET("api/v1/subjects")
    suspend fun getSubjects(): Response<SubjectsResponse>

    @GET("api/v1/subjects/{subject}/topics")
    suspend fun getTopicsBySubject(
        @Path("subject") subject: String
    ): Response<TopicsResponse>

    // Health Check
    @GET("api/v1/health")
    suspend fun healthCheck(): Response<HealthResponse>
}