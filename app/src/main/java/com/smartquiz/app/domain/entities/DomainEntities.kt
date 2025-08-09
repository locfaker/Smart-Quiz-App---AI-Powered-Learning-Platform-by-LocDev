package com.smartquiz.app.domain.entities

import java.time.LocalDateTime
import java.util.UUID

/**
 * Domain entities representing core business logic
 * These are independent of any framework or external concerns
 */

data class QuizDomain(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val subject: Subject,
    val difficulty: Difficulty,
    val questions: List<QuestionDomain>,
    val timeLimit: Int, // minutes
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime? = null,
    val score: Score,
    val feedback: AiFeedback? = null,
    val metadata: QuizMetadata
) {
    val isCompleted: Boolean get() = completedAt != null
    val duration: Long get() = if (completedAt != null && startedAt != null) {
        java.time.Duration.between(startedAt, completedAt).toMillis()
    } else 0L
}

data class QuestionDomain(
    val id: String = UUID.randomUUID().toString(),
    val subject: Subject,
    val difficulty: Difficulty,
    val type: QuestionType,
    val content: QuestionContent,
    val correctAnswer: Answer,
    val explanation: String,
    val hints: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val metadata: QuestionMetadata
)

data class QuestionContent(
    val text: String,
    val options: List<String>,
    val mediaUrls: List<String> = emptyList()
)

data class Answer(
    val index: Int? = null,
    val text: String? = null,
    val isCorrect: Boolean = false
)

data class UserAnswer(
    val questionId: String,
    val answer: Answer,
    val timeSpent: Long, // milliseconds
    val hintsUsed: Int = 0,
    val confidence: ConfidenceLevel? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class Score(
    val correct: Int,
    val total: Int,
    val points: Int,
    val percentage: Double = if (total > 0) (correct.toDouble() / total) * 100 else 0.0,
    val grade: Grade = Grade.fromPercentage(percentage)
)

data class AiFeedback(
    val overall: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>,
    val nextSteps: List<String>,
    val estimatedStudyTime: Int, // minutes
    val confidence: Double // 0.0 to 1.0
)

data class QuizMetadata(
    val source: String, // "ai_generated", "curated", "user_created"
    val version: String,
    val language: String = "vi",
    val estimatedDuration: Int, // minutes
    val prerequisites: List<String> = emptyList()
)

data class QuestionMetadata(
    val source: String,
    val author: String? = null,
    val reviewedBy: String? = null,
    val lastUpdated: LocalDateTime,
    val difficulty_score: Double, // 0.0 to 1.0
    val usage_count: Int = 0,
    val success_rate: Double = 0.0
)

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    val displayName: String,
    val avatar: String? = null,
    val level: Int = 1,
    val experience: Experience,
    val preferences: UserPreferences,
    val statistics: UserStatistics,
    val achievements: List<Achievement> = emptyList(),
    val createdAt: LocalDateTime,
    val lastActiveAt: LocalDateTime
)

data class Experience(
    val totalXp: Int = 0,
    val currentLevelXp: Int = 0,
    val nextLevelXp: Int = 100,
    val streak: Streak
)

data class Streak(
    val current: Int = 0,
    val longest: Int = 0,
    val lastActiveDate: LocalDateTime? = null
)

data class UserPreferences(
    val preferredSubjects: List<Subject> = emptyList(),
    val preferredDifficulty: Difficulty = Difficulty.MEDIUM,
    val dailyGoal: Int = 10, // questions per day
    val studyReminders: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val darkMode: Boolean = false
)

data class UserStatistics(
    val totalQuizzes: Int = 0,
    val totalQuestions: Int = 0,
    val totalCorrect: Int = 0,
    val totalTimeSpent: Long = 0, // milliseconds
    val averageAccuracy: Double = 0.0,
    val subjectStats: Map<Subject, SubjectStatistics> = emptyMap(),
    val weeklyProgress: List<DailyProgress> = emptyList()
)

data class SubjectStatistics(
    val subject: Subject,
    val quizzesCompleted: Int = 0,
    val averageScore: Double = 0.0,
    val timeSpent: Long = 0,
    val strongTopics: List<String> = emptyList(),
    val weakTopics: List<String> = emptyList()
)

data class DailyProgress(
    val date: LocalDateTime,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val timeSpent: Long,
    val xpEarned: Int
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val category: AchievementCategory,
    val requirement: AchievementRequirement,
    val unlockedAt: LocalDateTime? = null,
    val progress: Double = 0.0 // 0.0 to 1.0
) {
    val isUnlocked: Boolean get() = unlockedAt != null
}

data class AchievementRequirement(
    val type: String, // "quiz_count", "streak", "accuracy", "subject_mastery"
    val target: Int,
    val subject: Subject? = null
)

// Enums
enum class Subject(val displayName: String, val code: String) {
    MATHEMATICS("Toán học", "math"),
    PHYSICS("Vật lý", "physics"),
    CHEMISTRY("Hóa học", "chemistry"),
    BIOLOGY("Sinh học", "biology"),
    HISTORY("Lịch sử", "history"),
    GEOGRAPHY("Địa lý", "geography"),
    LITERATURE("Ngữ văn", "literature"),
    ENGLISH("Tiếng Anh", "english"),
    COMPUTER_SCIENCE("Tin học", "cs"),
    ECONOMICS("Kinh tế", "economics")
}

enum class Difficulty(val displayName: String, val level: Int) {
    BEGINNER("Cơ bản", 1),
    EASY("Dễ", 2),
    MEDIUM("Trung bình", 3),
    HARD("Khó", 4),
    EXPERT("Chuyên gia", 5);
    
    companion object {
        fun fromLevel(level: Int): Difficulty = values().find { it.level == level } ?: MEDIUM
    }
}

enum class QuestionType(val displayName: String) {
    MULTIPLE_CHOICE("Trắc nghiệm"),
    TRUE_FALSE("Đúng/Sai"),
    FILL_BLANK("Điền vào chỗ trống"),
    MATCHING("Nối câu"),
    ORDERING("Sắp xếp")
}

enum class ConfidenceLevel(val displayName: String, val value: Int) {
    VERY_LOW("Rất không chắc", 1),
    LOW("Không chắc", 2),
    MEDIUM("Bình thường", 3),
    HIGH("Chắc chắn", 4),
    VERY_HIGH("Rất chắc chắn", 5)
}

enum class Grade(val displayName: String, val minPercentage: Double) {
    F("Kém", 0.0),
    D("Yếu", 40.0),
    C("Trung bình", 55.0),
    B("Khá", 70.0),
    A("Giỏi", 85.0),
    A_PLUS("Xuất sắc", 95.0);
    
    companion object {
        fun fromPercentage(percentage: Double): Grade {
            return values().reversed().find { percentage >= it.minPercentage } ?: F
        }
    }
}

enum class AchievementCategory(val displayName: String) {
    LEARNING("Học tập"),
    STREAK("Chuỗi ngày"),
    MASTERY("Thành thạo"),
    SOCIAL("Xã hội"),
    SPECIAL("Đặc biệt")
}