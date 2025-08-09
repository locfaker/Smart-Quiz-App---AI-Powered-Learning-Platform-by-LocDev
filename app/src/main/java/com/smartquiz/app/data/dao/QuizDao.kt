package com.smartquiz.app.data.dao

import androidx.room.*
import com.smartquiz.app.data.entities.Quiz
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Query("SELECT * FROM quizzes ORDER BY completedAt DESC")
    fun getAllQuizzes(): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE subject = :subject ORDER BY completedAt DESC")
    fun getQuizzesBySubject(subject: String): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    suspend fun getQuizById(id: Long): Quiz?

    @Query("SELECT AVG(score) FROM quizzes WHERE subject = :subject")
    suspend fun getAverageScoreBySubject(subject: String): Double?

    @Query("SELECT COUNT(*) FROM quizzes WHERE subject = :subject")
    suspend fun getQuizCountBySubject(subject: String): Int

    @Query("SELECT * FROM quizzes ORDER BY score DESC LIMIT :limit")
    suspend fun getTopScores(limit: Int): List<Quiz>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz): Long

    @Update
    suspend fun updateQuiz(quiz: Quiz)

    @Delete
    suspend fun deleteQuiz(quiz: Quiz)

    @Query("DELETE FROM quizzes WHERE subject = :subject")
    suspend fun deleteQuizzesBySubject(subject: String)
}