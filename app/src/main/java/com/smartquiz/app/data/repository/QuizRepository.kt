package com.smartquiz.app.data.repository

import com.smartquiz.app.data.dao.QuizDao
import com.smartquiz.app.data.entities.Quiz
import kotlinx.coroutines.flow.Flow

class QuizRepository(
    private val quizDao: QuizDao
) {

    fun getAllQuizzes(): Flow<List<Quiz>> = quizDao.getAllQuizzes()

    fun getQuizzesBySubject(subject: String): Flow<List<Quiz>> = 
        quizDao.getQuizzesBySubject(subject)

    suspend fun getQuizById(id: Long): Quiz? = quizDao.getQuizById(id)

    suspend fun getAverageScoreBySubject(subject: String): Double? = 
        quizDao.getAverageScoreBySubject(subject)

    suspend fun getQuizCountBySubject(subject: String): Int = 
        quizDao.getQuizCountBySubject(subject)

    suspend fun getTopScores(limit: Int): List<Quiz> = quizDao.getTopScores(limit)

    suspend fun insertQuiz(quiz: Quiz): Long = quizDao.insertQuiz(quiz)

    suspend fun updateQuiz(quiz: Quiz) = quizDao.updateQuiz(quiz)

    suspend fun deleteQuiz(quiz: Quiz) = quizDao.deleteQuiz(quiz)

    suspend fun deleteQuizzesBySubject(subject: String) = 
        quizDao.deleteQuizzesBySubject(subject)
}
