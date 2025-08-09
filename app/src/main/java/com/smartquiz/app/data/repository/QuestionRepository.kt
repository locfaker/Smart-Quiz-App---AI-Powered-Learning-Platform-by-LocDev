package com.smartquiz.app.data.repository

import com.smartquiz.app.data.dao.QuestionDao
import com.smartquiz.app.data.entities.Question

class QuestionRepository(
    private val questionDao: QuestionDao
) {

    suspend fun getRandomQuestionsBySubjectAndDifficulty(
        subject: String,
        difficulty: String,
        limit: Int
    ): List<Question> = questionDao.getRandomQuestionsBySubjectAndDifficulty(subject, difficulty, limit)

    suspend fun getRandomQuestionsBySubject(subject: String, limit: Int): List<Question> = 
        questionDao.getRandomQuestionsBySubject(subject, limit)

    suspend fun getQuestionById(id: Long): Question? = questionDao.getQuestionById(id)

    suspend fun getAllSubjects(): List<String> = questionDao.getAllSubjects()

    suspend fun getQuestionCountBySubject(subject: String): Int = 
        questionDao.getQuestionCountBySubject(subject)

    suspend fun insertQuestion(question: Question): Long = questionDao.insertQuestion(question)

    suspend fun insertQuestions(questions: List<Question>) = questionDao.insertQuestions(questions)

    suspend fun updateQuestion(question: Question) = questionDao.updateQuestion(question)

    suspend fun deleteQuestion(question: Question) = questionDao.deleteQuestion(question)

    suspend fun deleteQuestionsBySubject(subject: String) = 
        questionDao.deleteQuestionsBySubject(subject)
}
