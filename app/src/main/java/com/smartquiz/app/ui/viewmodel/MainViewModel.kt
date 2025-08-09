package com.smartquiz.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smartquiz.app.data.database.AppDatabase
import com.smartquiz.app.data.entities.Quiz
import com.smartquiz.app.data.repository.QuestionRepository
import com.smartquiz.app.data.repository.QuizRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val quizRepository = QuizRepository(database.quizDao())
    private val questionRepository = QuestionRepository(database.questionDao())

    private val _recentQuizzes = MutableLiveData<List<Quiz>>()
    val recentQuizzes: LiveData<List<Quiz>> = _recentQuizzes

    private val _availableSubjects = MutableLiveData<List<String>>()
    val availableSubjects: LiveData<List<String>> = _availableSubjects

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadRecentQuizzes()
        loadAvailableSubjects()
    }

    private fun loadRecentQuizzes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val topScores = quizRepository.getTopScores(5)
                _recentQuizzes.value = topScores
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadAvailableSubjects() {
        viewModelScope.launch {
            try {
                val subjects = questionRepository.getAllSubjects()
                _availableSubjects.value = subjects.ifEmpty {
                    // Default subjects if no questions in database
                    listOf("Toán học", "Vật lý", "Hóa học", "Sinh học", "Văn học", "Tiếng Anh", "Lịch sử", "Địa lý")
                }
            } catch (e: Exception) {
                _error.value = e.message
                // Fallback to default subjects
                _availableSubjects.value = listOf("Toán học", "Vật lý", "Hóa học", "Sinh học", "Văn học", "Tiếng Anh", "Lịch sử", "Địa lý")
            }
        }
    }

    fun refreshData() {
        loadRecentQuizzes()
        loadAvailableSubjects()
    }

    fun clearError() {
        _error.value = null
    }
}
