package com.smartquiz.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.smartquiz.app.data.database.Converters
import java.util.UUID

@Entity(
    tableName = "quizzes",
    indices = [
        Index(value = ["subject"]),
        Index(value = ["difficulty"]),
        Index(value = ["completedAt"]),
        Index(value = ["score"])
    ]
)
@TypeConverters(Converters::class)
data class Quiz(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val difficulty: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Double,
    val percentage: Double = if (totalQuestions > 0) (correctAnswers.toDouble() / totalQuestions) * 100 else 0.0,
    val timeSpent: Long, // in milliseconds
    val timeLimitMinutes: Int,
    val aiFeedback: String? = null,
    val suggestions: List<String> = emptyList(),
    val completedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true,
    val isSynced: Boolean = false
)