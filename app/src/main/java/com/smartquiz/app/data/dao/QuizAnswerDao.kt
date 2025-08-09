package com.smartquiz.app.data.dao

import androidx.room.*
import com.smartquiz.app.data.entities.QuizAnswer
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAnswerDao {

    @Query("SELECT * FROM quiz_answers WHERE quizId = :quizId ORDER BY id")
    fun getAnswersByQuizId(quizId: Long): Flow<List<QuizAnswer>>

    @Query("SELECT * FROM quiz_answers WHERE quizId = :quizId AND questionId = :questionId")
    suspend fun getAnswerByQuizAndQuestion(quizId: Long, questionId: Long): QuizAnswer?

    @Query("SELECT COUNT(*) FROM quiz_answers WHERE quizId = :quizId AND isCorrect = 1")
    suspend fun getCorrectAnswersCount(quizId: Long): Int

    @Query("SELECT SUM(timeSpent) FROM quiz_answers WHERE quizId = :quizId")
    suspend fun getTotalTimeSpent(quizId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: QuizAnswer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<QuizAnswer>)

    @Update
    suspend fun updateAnswer(answer: QuizAnswer)

    @Delete
    suspend fun deleteAnswer(answer: QuizAnswer)

    @Query("DELETE FROM quiz_answers WHERE quizId = :quizId")
    suspend fun deleteAnswersByQuizId(quizId: Long)
}
