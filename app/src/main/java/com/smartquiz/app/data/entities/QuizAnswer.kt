package com.smartquiz.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "quiz_answers",
    foreignKeys = [
        ForeignKey(
            entity = Quiz::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["quizId"]),
        Index(value = ["questionId"]),
        Index(value = ["isCorrect"]),
        Index(value = ["answeredAt"])
    ]
)
data class QuizAnswer(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val quizId: String,
    val questionId: String,
    val userAnswerIndex: Int?, // Index of selected answer
    val userAnswerText: String?, // For fill_blank questions
    val isCorrect: Boolean,
    val pointsEarned: Int = 0,
    val timeSpent: Long, // in milliseconds
    val hintsUsed: Int = 0,
    val answeredAt: Long = System.currentTimeMillis(),
    val confidence: Int? = null, // 1-5 scale, how confident user was
    val flaggedForReview: Boolean = false
)