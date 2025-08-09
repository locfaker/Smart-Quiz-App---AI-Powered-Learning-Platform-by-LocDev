package com.smartquiz.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Base Response
data class BaseResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val error: ApiError?
)

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

// Question Generation
data class GenerateQuestionsRequest(
    val subject: String,
    val difficulty: String,
    val count: Int,
    val topics: List<String>? = null,
    val questionType: String = "multiple_choice",
    val language: String = "vi"
)

data class GenerateQuestionsResponse(
    val questions: List<ApiQuestion>,
    val generationId: String,
    val metadata: GenerationMetadata
)

data class GenerationMetadata(
    val model: String,
    val timestamp: Long,
    val processingTime: Long,
    val confidence: Double
)

data class ApiQuestion(
    val id: String,
    val subject: String,
    val difficulty: String,
    val questionType: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val hints: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val points: Int = 1,
    val timeLimit: Int? = null
)

// Questions Response
data class QuestionsResponse(
    val questions: List<ApiQuestion>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)

data class QuestionResponse(
    val question: ApiQuestion
)

// Feedback Generation
data class GenerateFeedbackRequest(
    val quizId: String,
    val subject: String,
    val difficulty: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeSpent: Long,
    val answers: List<QuizAnswerData>,
    val language: String = "vi"
)

data class QuizAnswerData(
    val questionId: String,
    val userAnswerIndex: Int?,
    val isCorrect: Boolean,
    val timeSpent: Long
)

data class GenerateFeedbackResponse(
    val feedback: String,
    val suggestions: List<String>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendedTopics: List<String>,
    val nextDifficulty: String?,
    val score: FeedbackScore
)

data class FeedbackScore(
    val overall: Double,
    val accuracy: Double,
    val speed: Double,
    val consistency: Double
)

// User Management
data class RegisterUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val user: ApiUser,
    val token: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class UserResponse(
    val user: ApiUser
)

data class ApiUser(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val level: Int,
    val totalXp: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val createdAt: Long,
    val lastActiveAt: Long
)

data class UserProfileResponse(
    val profile: ApiUser,
    val stats: UserStats
)

data class UserStats(
    val totalQuizzes: Int,
    val totalCorrectAnswers: Int,
    val totalTimeSpent: Long,
    val averageAccuracy: Double,
    val favoriteSubjects: List<String>,
    val achievements: List<Achievement>
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val unlockedAt: Long
)

data class UpdateUserProfileRequest(
    val displayName: String?,
    val avatarUrl: String?,
    val preferredDifficulty: String?,
    val dailyGoal: Int?
)

// Quiz Sync
data class SyncQuizzesRequest(
    val quizzes: List<SyncQuizData>,
    val lastSyncTimestamp: Long
)

data class SyncQuizData(
    val id: String,
    val subject: String,
    val difficulty: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Double,
    val timeSpent: Long,
    val completedAt: Long,
    val answers: List<QuizAnswerData>
)

data class SyncQuizzesResponse(
    val syncedCount: Int,
    val conflicts: List<SyncConflict>,
    val serverQuizzes: List<SyncQuizData>,
    val lastSyncTimestamp: Long
)

data class SyncConflict(
    val quizId: String,
    val reason: String,
    val clientData: SyncQuizData,
    val serverData: SyncQuizData
)

// Leaderboard
data class LeaderboardResponse(
    val leaderboard: List<LeaderboardEntry>,
    val userRank: Int?,
    val totalUsers: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val user: LeaderboardUser,
    val score: Double,
    val quizzesCompleted: Int
)

data class LeaderboardUser(
    val id: String,
    val displayName: String,
    val avatarUrl: String?,
    val level: Int
)

// Analytics
data class QuizCompletedRequest(
    val quizId: String,
    val subject: String,
    val difficulty: String,
    val score: Double,
    val timeSpent: Long,
    val completedAt: Long
)

data class AnalyticsResponse(
    val recorded: Boolean,
    val message: String?
)

data class UserStatsResponse(
    val stats: DetailedUserStats
)

data class DetailedUserStats(
    val totalQuizzes: Int,
    val totalCorrectAnswers: Int,
    val totalTimeSpent: Long,
    val averageAccuracy: Double,
    val subjectStats: Map<String, SubjectStats>,
    val dailyStats: List<DailyStats>,
    val streakInfo: StreakInfo
)

data class SubjectStats(
    val subject: String,
    val quizzesCompleted: Int,
    val averageScore: Double,
    val totalTimeSpent: Long,
    val bestStreak: Int
)

data class DailyStats(
    val date: String, // YYYY-MM-DD
    val quizzesCompleted: Int,
    val correctAnswers: Int,
    val timeSpent: Long
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: String
)

// Content Management
data class SubjectsResponse(
    val subjects: List<Subject>
)

@Parcelize
data class Subject(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val iconUrl: String?,
    val color: String,
    val isActive: Boolean,
    val topicCount: Int
) : Parcelable

data class TopicsResponse(
    val topics: List<Topic>
)

@Parcelize
data class Topic(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val subject: String,
    val difficulty: String,
    val questionCount: Int,
    val isActive: Boolean
) : Parcelable

// Health Check
data class HealthResponse(
    val status: String,
    val timestamp: Long,
    val version: String,
    val services: Map<String, ServiceHealth>
)

data class ServiceHealth(
    val status: String,
    val responseTime: Long,
    val lastCheck: Long
)