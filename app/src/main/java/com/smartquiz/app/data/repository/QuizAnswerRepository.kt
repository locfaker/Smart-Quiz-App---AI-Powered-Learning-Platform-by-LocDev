package com.smartquiz.app.data.repository

import com.smartquiz.app.data.dao.QuizAnswerDao
import com.smartquiz.app.data.entities.QuizAnswer
import kotlinx.coroutines.flow.Flow

class QuizAnswerRepository(
    private val quizAnswerDao: QuizAnswerDao
) {

    fun getAnswersByQuizId(quizId: Long): Flow<List<QuizAnswer>> = 
        quizAnswerDao.getAnswersByQuizId(quizId)

    suspend fun getAnswerByQuizAndQuestion(quizId: Long, questionId: Long): QuizAnswer? = 
        quizAnswerDao.getAnswerByQuizAndQuestion(quizId, questionId)

    suspend fun getCorrectAnswersCount(quizId: Long): Int = 
        quizAnswerDao.getCorrectAnswersCount(quizId)

    suspend fun getTotalTimeSpent(quizId: Long): Long? = 
        quizAnswerDao.getTotalTimeSpent(quizId)

    suspend fun insertAnswer(answer: QuizAnswer): Long = quizAnswerDao.insertAnswer(answer)

    suspend fun insertAnswers(answers: List<QuizAnswer>) = quizAnswerDao.insertAnswers(answers)

    suspend fun updateAnswer(answer: QuizAnswer) = quizAnswerDao.updateAnswer(answer)

    suspend fun deleteAnswer(answer: QuizAnswer) = quizAnswerDao.deleteAnswer(answer)

    suspend fun deleteAnswersByQuizId(quizId: Long) = quizAnswerDao.deleteAnswersByQuizId(quizId)
}
