package com.smartquiz.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smartquiz.app.data.database.AppDatabase
import com.smartquiz.app.data.repository.QuestionRepository
import com.smartquiz.app.data.repository.QuizRepository
import kotlinx.coroutines.launch

data class SubjectInfo(
    val name: String,
    val questionCount: Int,
    val averageScore: Double?,
    val quizCount: Int
)

class SubjectListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val questionRepository = QuestionRepository(database.questionDao())
    private val quizRepository = QuizRepository(database.quizDao())

    private val _subjects = MutableLiveData<List<SubjectInfo>>()
    val subjects: LiveData<List<SubjectInfo>> = _subjects

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val availableSubjects = questionRepository.getAllSubjects()
                val defaultSubjects = listOf("Toán học", "Vật lý", "Hóa học", "Sinh học", "Văn học", "Tiếng Anh", "Lịch sử", "Địa lý")
                
                val allSubjects = if (availableSubjects.isNotEmpty()) availableSubjects else defaultSubjects
                
                val subjectInfoList = allSubjects.map { subject ->
                    val questionCount = questionRepository.getQuestionCountBySubject(subject)
                    val averageScore = quizRepository.getAverageScoreBySubject(subject)
                    val quizCount = quizRepository.getQuizCountBySubject(subject)
                    
                    SubjectInfo(
                        name = subject,
                        questionCount = questionCount,
                        averageScore = averageScore,
                        quizCount = quizCount
                    )
                }
                
                _subjects.value = subjectInfoList
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshSubjects() {
        loadSubjects()
    }

    fun clearError() {
        _error.value = null
    }
}
