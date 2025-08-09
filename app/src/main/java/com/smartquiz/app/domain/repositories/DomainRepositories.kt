package com.smartquiz.app.domain.repositories

import com.smartquiz.app.domain.entities.*
import com.smartquiz.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interfaces for Domain layer
 * These define contracts for data access without implementation details
 */

interface QuizRepository {
    suspend fun saveQuiz(quiz: QuizDomain): Resource<String>
    suspend fun updateQuiz(quiz: QuizDomain): Resource<Unit>
    suspend fun getQuizById(quizId: String): QuizDomain?
    fun getQuizHistory(userId: String): Flow<List<QuizDomain>>
    suspend fun getRecentQuizzes(userId: String, limit: Int): List<QuizDomain>
    suspend fun getQuizzesBySubject(userId: String, subject: Subject): List<QuizDomain>
    suspend fun deleteQuiz(quizId: String): Resource<Unit>
    suspend fun syncQuizzes(userId: String): Resource<Unit>
}

interface UserRepository {
    suspend fun createUserProfile(user: UserProfile): Resource<String>
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun updateUserProfile(user: UserProfile): Resource<Unit>
    suspend fun getUserByUsername(username: String): UserProfile?
    suspend fun getUserByEmail(email: String): UserProfile?
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    suspend fun updateUserExperience(userId: String, xpGained: Int): Resource<Unit>
    suspend fun updateUserStreak(userId: String): Resource<Unit>
    suspend fun addAchievement(userId: String, achievement: Achievement): Resource<Unit>
    suspend fun deleteUserProfile(userId: String): Resource<Unit>
}

interface AiRepository {
    suspend fun generateQuestions(
        subject: Subject,
        difficulty: Difficulty,
        count: Int,
        topics: List<String> = emptyList(),
        userLevel: Int = 1,
        weakAreas: List<String> = emptyList()
    ): Resource<List<QuestionDomain>>
    
    suspend fun generateFeedback(
        quiz: QuizDomain,
        answers: List<UserAnswer>,
        score: Score
    ): Resource<AiFeedback>
    
    suspend fun generatePersonalizedRecommendations(
        userProfile: UserProfile,
        recentQuizzes: List<QuizDomain>
    ): Resource<List<StudyRecommendation>>
    
    suspend fun generateExplanation(
        question: QuestionDomain,
        userAnswer: UserAnswer
    ): Resource<String>
    
    suspend fun validateQuestion(question: QuestionDomain): Resource<QuestionValidation>
}

interface ContentRepository {
    suspend fun getSubjects(): Resource<List<Subject>>
    suspend fun getTopicsBySubject(subject: Subject): Resource<List<String>>
    suspend fun searchQuestions(
        query: String,
        subject: Subject? = null,
        difficulty: Difficulty? = null
    ): Resource<List<QuestionDomain>>
    suspend fun getQuestionById(questionId: String): Resource<QuestionDomain>
    suspend fun reportQuestion(questionId: String, reason: String): Resource<Unit>
}

interface AnalyticsRepository {
    suspend fun trackQuizStarted(quiz: QuizDomain): Resource<Unit>
    suspend fun trackQuizCompleted(quiz: QuizDomain, answers: List<UserAnswer>): Resource<Unit>
    suspend fun trackUserAction(userId: String, action: String, metadata: Map<String, Any>): Resource<Unit>
    suspend fun getUserAnalytics(userId: String, timeframe: AnalyticsTimeframe): Resource<UserAnalytics>
    suspend fun getSystemAnalytics(timeframe: AnalyticsTimeframe): Resource<SystemAnalytics>
}

interface AuthRepository {
    suspend fun login(username: String, password: String): Resource<AuthResult>
    suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String
    ): Resource<AuthResult>
    suspend fun logout(): Resource<Unit>
    suspend fun refreshToken(): Resource<String>
    suspend fun getCurrentUser(): Resource<UserProfile>
    suspend fun resetPassword(email: String): Resource<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}

interface CacheRepository {
    suspend fun <T> get(key: String, type: Class<T>): T?
    suspend fun <T> put(key: String, value: T, ttlSeconds: Long = 3600): Unit
    suspend fun remove(key: String): Unit
    suspend fun clear(): Unit
    suspend fun getKeys(pattern: String): List<String>
}

interface SyncRepository {
    suspend fun syncUserData(userId: String): Resource<SyncResult>
    suspend fun syncQuizzes(userId: String): Resource<SyncResult>
    suspend fun syncAchievements(userId: String): Resource<SyncResult>
    suspend fun getLastSyncTime(userId: String): Long
    suspend fun setLastSyncTime(userId: String, timestamp: Long): Unit
    fun observeSyncStatus(): Flow<SyncStatus>
}

// Data classes for repository results
data class AuthResult(
    val user: UserProfile,
    val token: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class StudyRecommendation(
    val id: String,
    val type: RecommendationType,
    val subject: Subject,
    val difficulty: Difficulty,
    val title: String,
    val description: String,
    val estimatedTime: Int, // minutes
    val priority: Int, // 1-5, 5 being highest
    val reasons: List<String>
)

data class QuestionValidation(
    val isValid: Boolean,
    val issues: List<ValidationIssue>,
    val suggestions: List<String>,
    val confidence: Double
)

data class ValidationIssue(
    val type: String,
    val severity: IssueSeverity,
    val message: String,
    val field: String? = null
)

data class UserAnalytics(
    val userId: String,
    val timeframe: AnalyticsTimeframe,
    val totalQuizzes: Int,
    val totalQuestions: Int,
    val averageAccuracy: Double,
    val totalTimeSpent: Long,
    val subjectBreakdown: Map<Subject, SubjectAnalytics>,
    val performanceTrend: List<PerformancePoint>,
    val streakInfo: StreakAnalytics
)

data class SystemAnalytics(
    val timeframe: AnalyticsTimeframe,
    val totalUsers: Int,
    val activeUsers: Int,
    val totalQuizzes: Int,
    val averageSessionTime: Long,
    val popularSubjects: List<SubjectPopularity>,
    val userRetention: RetentionMetrics
)

data class SubjectAnalytics(
    val subject: Subject,
    val quizzesCompleted: Int,
    val averageScore: Double,
    val timeSpent: Long,
    val improvement: Double
)

data class PerformancePoint(
    val timestamp: Long,
    val accuracy: Double,
    val speed: Double,
    val confidence: Double
)

data class StreakAnalytics(
    val currentStreak: Int,
    val longestStreak: Int,
    val streakHistory: List<StreakPoint>
)

data class StreakPoint(
    val date: Long,
    val streakLength: Int,
    val questionsAnswered: Int
)

data class SubjectPopularity(
    val subject: Subject,
    val userCount: Int,
    val quizCount: Int,
    val averageRating: Double
)

data class RetentionMetrics(
    val day1: Double,
    val day7: Double,
    val day30: Double,
    val day90: Double
)

data class SyncResult(
    val success: Boolean,
    val itemsSynced: Int,
    val conflicts: List<SyncConflict>,
    val errors: List<String>
)

data class SyncConflict(
    val itemId: String,
    val itemType: String,
    val localVersion: Long,
    val remoteVersion: Long,
    val resolution: ConflictResolution
)

// Enums
enum class RecommendationType {
    REVIEW_WEAK_AREAS,
    PRACTICE_NEW_TOPIC,
    INCREASE_DIFFICULTY,
    MAINTAIN_STREAK,
    EXPLORE_SUBJECT
}

enum class IssueSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class AnalyticsTimeframe {
    DAY, WEEK, MONTH, QUARTER, YEAR, ALL_TIME
}

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

enum class ConflictResolution {
    USE_LOCAL, USE_REMOTE, MERGE, MANUAL
}