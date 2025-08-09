package com.smartquiz.app.data.dao

import androidx.room.*
import com.smartquiz.app.data.entities.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Query("SELECT * FROM questions WHERE subject = :subject AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsBySubjectAndDifficulty(
        subject: String,
        difficulty: String,
        limit: Int
    ): List<Question>

    @Query("SELECT * FROM questions WHERE subject = :subject ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsBySubject(subject: String, limit: Int): List<Question>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): Question?

    @Query("SELECT DISTINCT subject FROM questions")
    suspend fun getAllSubjects(): List<String>

    @Query("SELECT COUNT(*) FROM questions WHERE subject = :subject")
    suspend fun getQuestionCountBySubject(subject: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions WHERE subject = :subject")
    suspend fun deleteQuestionsBySubject(subject: String)
}