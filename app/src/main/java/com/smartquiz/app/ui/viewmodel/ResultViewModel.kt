package com.smartquiz.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smartquiz.app.data.database.AppDatabase
import com.smartquiz.app.data.entities.Quiz
import com.smartquiz.app.data.entities.QuizAnswer
import com.smartquiz.app.data.repository.QuizAnswerRepository
import com.smartquiz.app.data.repository.QuizRepository
import kotlinx.coroutines.launch

data class QuizResult(
    val quiz: Quiz,
    val answers: List<QuizAnswer>
)

class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val quizRepository = QuizRepository(database.quizDao())
    private val quizAnswerRepository = QuizAnswerRepository(database.quizAnswerDao())

    private val _quizResult = MutableLiveData<QuizResult?>()
    val quizResult: LiveData<QuizResult?> = _quizResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadQuizResult(quizId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val quiz = quizRepository.getQuizById(quizId)
                if (quiz != null) {
                    // Get answers for this quiz
                    quizAnswerRepository.getAnswersByQuizId(quizId).collect { answers ->
                        _quizResult.value = QuizResult(quiz, answers)
                    }
                } else {
                    _error.value = "Không tìm thấy kết quả quiz"
                }
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getScorePercentage(): Int {
        val result = _quizResult.value ?: return 0
        return result.quiz.score.toInt()
    }

    fun getTimeSpentFormatted(): String {
        val result = _quizResult.value ?: return "0:00"
        val totalSeconds = result.quiz.timeSpent / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun getCorrectAnswersText(): String {
        val result = _quizResult.value ?: return "0/0"
        return "${result.quiz.correctAnswers}/${result.quiz.totalQuestions}"
    }

    fun clearError() {
        _error.value = null
    }
}
