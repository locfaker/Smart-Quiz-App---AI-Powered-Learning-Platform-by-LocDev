package com.smartquiz.app.domain.usecases

import com.smartquiz.app.domain.entities.*
import com.smartquiz.app.domain.repositories.QuizRepository
import com.smartquiz.app.domain.repositories.UserRepository
import com.smartquiz.app.domain.repositories.AiRepository
import com.smartquiz.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Cases for Quiz functionality following Clean Architecture principles
 * These contain the business logic and orchestrate data flow between layers
 */

class GenerateQuizUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        subject: Subject,
        difficulty: Difficulty,
        questionCount: Int,
        timeLimit: Int,
        topics: List<String> = emptyList()
    ): Resource<QuizDomain> {
        return try {
            // Get user preferences to personalize quiz
            val userProfile = userRepository.getUserProfile(userId)
            
            // Generate questions using AI
            val questionsResult = aiRepository.generateQuestions(
                subject = subject,
                difficulty = difficulty,
                count = questionCount,
                topics = topics,
                userLevel = userProfile?.level ?: 1,
                weakAreas = userProfile?.statistics?.subjectStats?.get(subject)?.weakTopics ?: emptyList()
            )
            
            when (questionsResult) {
                is Resource.Success -> {
                    val questions = questionsResult.data ?: emptyList()
                    
                    val quiz = QuizDomain(
                        userId = userId,
                        subject = subject,
                        difficulty = difficulty,
                        questions = questions,
                        timeLimit = timeLimit,
                        startedAt = java.time.LocalDateTime.now(),
                        score = Score(0, questions.size, 0),
                        metadata = QuizMetadata(
                            source = "ai_generated",
                            version = "1.0",
                            estimatedDuration = timeLimit
                        )
                    )
                    
                    // Save quiz to repository
                    quizRepository.saveQuiz(quiz)
                    Resource.Success(quiz)
                }
                is Resource.Error -> Resource.Error(questionsResult.message ?: "Failed to generate questions")
                is Resource.Loading -> Resource.Loading()
            }
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.message}")
        }
    }
}

class SubmitQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository,
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(
        quizId: String,
        answers: List<UserAnswer>
    ): Resource<QuizResult> {
        return try {
            val quiz = quizRepository.getQuizById(quizId)
                ?: return Resource.Error("Quiz not found")
            
            // Calculate score
            val correctAnswers = answers.count { userAnswer ->
                val question = quiz.questions.find { it.id == userAnswer.questionId }
                question?.let { q ->
                    when (q.type) {
                        QuestionType.MULTIPLE_CHOICE -> userAnswer.answer.index == q.correctAnswer.index
                        QuestionType.TRUE_FALSE -> userAnswer.answer.index == q.correctAnswer.index
                        QuestionType.FILL_BLANK -> userAnswer.answer.text?.lowercase()?.trim() == 
                                                  q.correctAnswer.text?.lowercase()?.trim()
                        else -> false
                    }
                } ?: false
            }
            
            val totalPoints = correctAnswers * 10 // Base points per correct answer
            val bonusPoints = calculateBonusPoints(answers, quiz)
            val finalScore = Score(
                correct = correctAnswers,
                total = quiz.questions.size,
                points = totalPoints + bonusPoints
            )
            
            // Generate AI feedback
            val feedbackResult = aiRepository.generateFeedback(
                quiz = quiz,
                answers = answers,
                score = finalScore
            )
            
            val feedback = when (feedbackResult) {
                is Resource.Success -> feedbackResult.data
                else -> null
            }
            
            // Update quiz with results
            val completedQuiz = quiz.copy(
                completedAt = java.time.LocalDateTime.now(),
                score = finalScore,
                feedback = feedback
            )
            
            // Save completed quiz
            quizRepository.updateQuiz(completedQuiz)
            
            // Update user statistics
            updateUserStatistics(quiz.userId, completedQuiz, answers)
            
            // Award achievements
            val newAchievements = checkAndAwardAchievements(quiz.userId, completedQuiz)
            
            Resource.Success(
                QuizResult(
                    quiz = completedQuiz,
                    answers = answers,
                    newAchievements = newAchievements
                )
            )
        } catch (e: Exception) {
            Resource.Error("Failed to submit quiz: ${e.message}")
        }
    }
    
    private fun calculateBonusPoints(answers: List<UserAnswer>, quiz: QuizDomain): Int {
        var bonus = 0
        
        // Speed bonus
        val avgTimePerQuestion = answers.map { it.timeSpent }.average()
        val expectedTime = (quiz.timeLimit * 60 * 1000) / quiz.questions.size.toDouble()
        if (avgTimePerQuestion < expectedTime * 0.7) {
            bonus += 20 // Speed bonus
        }
        
        // Confidence bonus
        val highConfidenceCount = answers.count { 
            it.confidence == ConfidenceLevel.HIGH || it.confidence == ConfidenceLevel.VERY_HIGH 
        }
        if (highConfidenceCount > quiz.questions.size * 0.8) {
            bonus += 15 // Confidence bonus
        }
        
        // No hints bonus
        val noHintsCount = answers.count { it.hintsUsed == 0 }
        if (noHintsCount == quiz.questions.size) {
            bonus += 10 // No hints bonus
        }
        
        return bonus
    }
    
    private suspend fun updateUserStatistics(userId: String, quiz: QuizDomain, answers: List<UserAnswer>) {
        val user = userRepository.getUserProfile(userId) ?: return
        
        val newStats = user.statistics.copy(
            totalQuizzes = user.statistics.totalQuizzes + 1,
            totalQuestions = user.statistics.totalQuestions + quiz.questions.size,
            totalCorrect = user.statistics.totalCorrect + quiz.score.correct,
            totalTimeSpent = user.statistics.totalTimeSpent + quiz.duration,
            averageAccuracy = calculateNewAverageAccuracy(user.statistics, quiz.score)
        )
        
        // Update XP
        val xpGained = calculateXpGained(quiz, answers)
        val newExperience = user.experience.copy(
            totalXp = user.experience.totalXp + xpGained
        )
        
        val updatedUser = user.copy(
            statistics = newStats,
            experience = newExperience,
            lastActiveAt = java.time.LocalDateTime.now()
        )
        
        userRepository.updateUserProfile(updatedUser)
    }
    
    private fun calculateNewAverageAccuracy(stats: UserStatistics, newScore: Score): Double {
        val totalCorrect = stats.totalCorrect + newScore.correct
        val totalQuestions = stats.totalQuestions + newScore.total
        return if (totalQuestions > 0) (totalCorrect.toDouble() / totalQuestions) * 100 else 0.0
    }
    
    private fun calculateXpGained(quiz: QuizDomain, answers: List<UserAnswer>): Int {
        val baseXp = quiz.score.correct * 10
        val difficultyMultiplier = when (quiz.difficulty) {
            Difficulty.BEGINNER -> 1.0
            Difficulty.EASY -> 1.2
            Difficulty.MEDIUM -> 1.5
            Difficulty.HARD -> 2.0
            Difficulty.EXPERT -> 2.5
        }
        return (baseXp * difficultyMultiplier).toInt()
    }
    
    private suspend fun checkAndAwardAchievements(userId: String, quiz: QuizDomain): List<Achievement> {
        // Implementation for achievement system
        return emptyList() // Placeholder
    }
}

class GetQuizHistoryUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(userId: String): Flow<List<QuizDomain>> {
        return quizRepository.getQuizHistory(userId)
    }
}

class GetQuizStatisticsUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Resource<QuizStatistics> {
        return try {
            val user = userRepository.getUserProfile(userId)
                ?: return Resource.Error("User not found")
            
            val recentQuizzes = quizRepository.getRecentQuizzes(userId, 30) // Last 30 quizzes
            
            val statistics = QuizStatistics(
                totalQuizzes = user.statistics.totalQuizzes,
                averageScore = user.statistics.averageAccuracy,
                totalTimeSpent = user.statistics.totalTimeSpent,
                strongestSubject = findStrongestSubject(user.statistics.subjectStats),
                weakestSubject = findWeakestSubject(user.statistics.subjectStats),
                recentPerformance = calculateRecentPerformance(recentQuizzes),
                improvementTrend = calculateImprovementTrend(recentQuizzes)
            )
            
            Resource.Success(statistics)
        } catch (e: Exception) {
            Resource.Error("Failed to get statistics: ${e.message}")
        }
    }
    
    private fun findStrongestSubject(subjectStats: Map<Subject, SubjectStatistics>): Subject? {
        return subjectStats.maxByOrNull { it.value.averageScore }?.key
    }
    
    private fun findWeakestSubject(subjectStats: Map<Subject, SubjectStatistics>): Subject? {
        return subjectStats.minByOrNull { it.value.averageScore }?.key
    }
    
    private fun calculateRecentPerformance(quizzes: List<QuizDomain>): Double {
        return if (quizzes.isNotEmpty()) {
            quizzes.map { it.score.percentage }.average()
        } else 0.0
    }
    
    private fun calculateImprovementTrend(quizzes: List<QuizDomain>): Double {
        if (quizzes.size < 2) return 0.0
        
        val sortedQuizzes = quizzes.sortedBy { it.startedAt }
        val firstHalf = sortedQuizzes.take(sortedQuizzes.size / 2)
        val secondHalf = sortedQuizzes.drop(sortedQuizzes.size / 2)
        
        val firstHalfAvg = firstHalf.map { it.score.percentage }.average()
        val secondHalfAvg = secondHalf.map { it.score.percentage }.average()
        
        return secondHalfAvg - firstHalfAvg
    }
}

// Data classes for use case results
data class QuizResult(
    val quiz: QuizDomain,
    val answers: List<UserAnswer>,
    val newAchievements: List<Achievement>
)

data class QuizStatistics(
    val totalQuizzes: Int,
    val averageScore: Double,
    val totalTimeSpent: Long,
    val strongestSubject: Subject?,
    val weakestSubject: Subject?,
    val recentPerformance: Double,
    val improvementTrend: Double
)