package com.smartquiz.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smartquiz.app.data.database.AppDatabase
import com.smartquiz.app.data.entities.Question
import com.smartquiz.app.data.repository.QuestionRepository
import kotlinx.coroutines.launch

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val questionRepository = QuestionRepository(database.questionDao())

    private val _currentQuestion = MutableLiveData<Question?>()
    val currentQuestion: LiveData<Question?> = _currentQuestion

    private val _currentQuestionIndex = MutableLiveData<Int>()
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _isCompleted = MutableLiveData<Boolean>()
    val isCompleted: LiveData<Boolean> = _isCompleted

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var questions = listOf<Question>()
    private var currentIndex = 0
    private var userAnswers = mutableMapOf<Int, String>()

    fun startReview(subject: String, difficulty: String? = null) {
        viewModelScope.launch {
            try {
                questions = if (difficulty != null) {
                    questionRepository.getRandomQuestionsBySubjectAndDifficulty(subject, difficulty, 20)
                } else {
                    questionRepository.getRandomQuestionsBySubject(subject, 20)
                }

                if (questions.isNotEmpty()) {
                    currentIndex = 0
                    updateCurrentQuestion()
                } else {
                    _error.value = "Không có câu hỏi nào để ôn tập cho chủ đề này"
                }

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectAnswer(answer: String) {
        userAnswers[currentIndex] = answer
    }

    fun nextQuestion() {
        if (currentIndex < questions.size - 1) {
            currentIndex++
            updateCurrentQuestion()
        } else {
            _isCompleted.value = true
        }
    }

    fun previousQuestion() {
        if (currentIndex > 0) {
            currentIndex--
            updateCurrentQuestion()
        }
    }

    fun getCurrentCorrectAnswer(): String {
        return if (currentIndex < questions.size) {
            questions[currentIndex].correctAnswer
        } else {
            ""
        }
    }

    fun getCurrentUserAnswer(): String {
        return userAnswers[currentIndex] ?: ""
    }

    fun getTotalQuestions(): Int {
        return questions.size
    }

    fun getProgress(): Float {
        return if (questions.isNotEmpty()) {
            (currentIndex + 1).toFloat() / questions.size
        } else {
            0f
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun updateCurrentQuestion() {
        if (currentIndex < questions.size) {
            _currentQuestion.value = questions[currentIndex]
            _currentQuestionIndex.value = currentIndex
        }
    }
}
