package com.smartquiz.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smartquiz.app.data.database.AppDatabase
import com.smartquiz.app.data.repository.QuizRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val quizRepository = QuizRepository(database.quizDao())

    private val _totalQuizzes = MutableLiveData<Int>()
    val totalQuizzes: LiveData<Int> = _totalQuizzes

    private val _averageScore = MutableLiveData<Double>()
    val averageScore: LiveData<Double> = _averageScore

    private val _scoreDistribution = MutableLiveData<Map<String, Int>>()
    val scoreDistribution: LiveData<Map<String, Int>> = _scoreDistribution

    private val _subjectPerformance = MutableLiveData<Map<String, Double>>()
    val subjectPerformance: LiveData<Map<String, Double>> = _subjectPerformance

    private val _progressOverTime = MutableLiveData<List<Pair<String, Double>>>()
    val progressOverTime: LiveData<List<Pair<String, Double>>> = _progressOverTime

    fun loadStatistics() {
        viewModelScope.launch {
            try {
                // Load all quizzes
                quizRepository.getAllQuizzes().collect { quizzes ->
                    if (quizzes.isNotEmpty()) {
                        // Total quizzes
                        _totalQuizzes.value = quizzes.size

                        // Average score
                        val avgScore = quizzes.map { it.score }.average()
                        _averageScore.value = avgScore

                        // Score distribution
                        val distribution = mutableMapOf<String, Int>()
                        distribution["0-50%"] = quizzes.count { it.score < 50 }
                        distribution["50-70%"] = quizzes.count { it.score in 50.0..69.9 }
                        distribution["70-85%"] = quizzes.count { it.score in 70.0..84.9 }
                        distribution["85-100%"] = quizzes.count { it.score >= 85 }
                        _scoreDistribution.value = distribution

                        // Subject performance
                        val subjectPerf = quizzes.groupBy { it.subject }
                            .mapValues { (_, quizList) ->
                                quizList.map { it.score }.average()
                            }
                        _subjectPerformance.value = subjectPerf

                        // Progress over time (last 10 quizzes)
                        val recentQuizzes = quizzes.sortedBy { it.completedAt }.takeLast(10)
                        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                        val progress = recentQuizzes.map { quiz ->
                            val date = dateFormat.format(Date(quiz.completedAt))
                            Pair(date, quiz.score)
                        }
                        _progressOverTime.value = progress

                    } else {
                        // No data available
                        _totalQuizzes.value = 0
                        _averageScore.value = 0.0
                        _scoreDistribution.value = emptyMap()
                        _subjectPerformance.value = emptyMap()
                        _progressOverTime.value = emptyList()
                    }
                }

            } catch (e: Exception) {
                // Handle error
                _totalQuizzes.value = 0
                _averageScore.value = 0.0
                _scoreDistribution.value = emptyMap()
                _subjectPerformance.value = emptyMap()
                _progressOverTime.value = emptyList()
            }
        }
    }
}
