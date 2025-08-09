package com.smartquiz.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smartquiz.app.data.api.ApiClient
import com.smartquiz.app.data.database.AppDatabase
import com.smartquiz.app.data.entities.Question
import com.smartquiz.app.data.entities.Quiz
import com.smartquiz.app.data.entities.QuizAnswer
import com.smartquiz.app.data.models.WrongAnswer
import com.smartquiz.app.data.repository.ApiRepository
import com.smartquiz.app.data.repository.QuestionRepository
import com.smartquiz.app.data.repository.QuizAnswerRepository
import com.smartquiz.app.data.repository.QuizRepository
import kotlinx.coroutines.launch

data class QuizState(
    val currentQuestionIndex: Int = 0,
    val questions: List<Question> = emptyList(),
    val answers: MutableMap<Long, String> = mutableMapOf(),
    val timeSpent: MutableMap<Long, Long> = mutableMapOf(),
    val startTime: Long = System.currentTimeMillis(),
    val isFinished: Boolean = false
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val questionRepository = QuestionRepository(database.questionDao())
    private val quizRepository = QuizRepository(database.quizDao())
    private val quizAnswerRepository = QuizAnswerRepository(database.quizAnswerDao())
    private val apiRepository = ApiRepository(ApiClient.quizApiService)

    private val _quizState = MutableLiveData<QuizState>()
    val quizState: LiveData<QuizState> = _quizState

    private val _currentQuestion = MutableLiveData<Question?>()
    val currentQuestion: LiveData<Question?> = _currentQuestion

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _quizCompleted = MutableLiveData<Long?>()
    val quizCompleted: LiveData<Long?> = _quizCompleted

    private var questionStartTime = System.currentTimeMillis()

    fun startQuiz(subject: String, difficulty: String, questionCount: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Try to get questions from database first
                var questions = questionRepository.getRandomQuestionsBySubjectAndDifficulty(
                    subject, difficulty, questionCount
                )
                
                // If not enough questions in database, try to generate from API
                if (questions.size < questionCount) {
                    val apiResult = apiRepository.generateQuestions(subject, difficulty, questionCount)
                    if (apiResult.isSuccess) {
                        val generatedQuestions = apiResult.getOrNull() ?: emptyList()
                        // Save generated questions to database
                        questionRepository.insertQuestions(generatedQuestions)
                        questions = generatedQuestions.take(questionCount)
                    }
                }
                
                if (questions.isEmpty()) {
                    _error.value = "Không có câu hỏi nào cho chủ đề này"
                    return@launch
                }
                
                val initialState = QuizState(
                    currentQuestionIndex = 0,
                    questions = questions.take(questionCount)
                )
                
                _quizState.value = initialState
                updateCurrentQuestion()
                questionStartTime = System.currentTimeMillis()
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun answerQuestion(answer: String) {
        val state = _quizState.value ?: return
        val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        
        val timeSpent = System.currentTimeMillis() - questionStartTime
        
        val updatedAnswers = state.answers.toMutableMap()
        updatedAnswers[currentQuestion.id] = answer
        
        val updatedTimeSpent = state.timeSpent.toMutableMap()
        updatedTimeSpent[currentQuestion.id] = timeSpent
        
        _quizState.value = state.copy(
            answers = updatedAnswers,
            timeSpent = updatedTimeSpent
        )
    }

    fun nextQuestion() {
        val state = _quizState.value ?: return
        
        if (state.currentQuestionIndex < state.questions.size - 1) {
            _quizState.value = state.copy(
                currentQuestionIndex = state.currentQuestionIndex + 1
            )
            updateCurrentQuestion()
            questionStartTime = System.currentTimeMillis()
        } else {
            finishQuiz()
        }
    }

    private fun updateCurrentQuestion() {
        val state = _quizState.value ?: return
        _currentQuestion.value = state.questions.getOrNull(state.currentQuestionIndex)
    }

    private fun finishQuiz() {
        val state = _quizState.value ?: return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val totalTimeSpent = state.timeSpent.values.sum()
                val correctAnswers = state.questions.count { question ->
                    state.answers[question.id] == question.correctAnswer
                }
                val score = (correctAnswers.toDouble() / state.questions.size) * 100
                
                // Generate AI feedback
                val wrongAnswers = state.questions.mapNotNull { question ->
                    val userAnswer = state.answers[question.id]
                    if (userAnswer != null && userAnswer != question.correctAnswer) {
                        WrongAnswer(
                            question = question.questionText,
                            correctAnswer = question.correctAnswer,
                            userAnswer = userAnswer
                        )
                    } else null
                }
                
                var aiFeedback = "Chúc mừng bạn đã hoàn thành bài quiz!"
                var suggestions = "Hãy tiếp tục luyện tập để cải thiện kết quả."
                
                if (state.questions.isNotEmpty()) {
                    val feedbackResult = apiRepository.generateFeedback(
                        subject = state.questions.first().subject,
                        difficulty = state.questions.first().difficulty,
                        totalQuestions = state.questions.size,
                        correctAnswers = correctAnswers,
                        timeSpent = totalTimeSpent,
                        wrongAnswers = wrongAnswers
                    )
                    
                    if (feedbackResult.isSuccess) {
                        val (feedback, suggestionText) = feedbackResult.getOrNull() ?: Pair(aiFeedback, suggestions)
                        aiFeedback = feedback
                        suggestions = suggestionText
                    }
                }
                
                // Save quiz result
                val quiz = Quiz(
                    subject = state.questions.firstOrNull()?.subject ?: "",
                    difficulty = state.questions.firstOrNull()?.difficulty ?: "",
                    totalQuestions = state.questions.size,
                    correctAnswers = correctAnswers,
                    score = score,
                    timeSpent = totalTimeSpent,
                    aiFeedback = aiFeedback,
                    suggestions = suggestions
                )
                
                val quizId = quizRepository.insertQuiz(quiz)
                
                // Save individual answers
                val quizAnswers = state.questions.map { question ->
                    val userAnswer = state.answers[question.id] ?: ""
                    val isCorrect = userAnswer == question.correctAnswer
                    val timeSpent = state.timeSpent[question.id] ?: 0L
                    
                    QuizAnswer(
                        quizId = quizId,
                        questionId = question.id,
                        userAnswer = userAnswer,
                        isCorrect = isCorrect,
                        timeSpent = timeSpent
                    )
                }
                
                quizAnswerRepository.insertAnswers(quizAnswers)
                
                _quizCompleted.value = quizId
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
