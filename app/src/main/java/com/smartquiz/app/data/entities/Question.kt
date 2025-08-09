package com.smartquiz.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.smartquiz.app.data.database.Converters
import java.util.UUID

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["subject"]),
        Index(value = ["difficulty"]),
        Index(value = ["questionType"]),
        Index(value = ["isActive"])
    ]
)
@TypeConverters(Converters::class)
data class Question(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val difficulty: String, // "easy", "medium", "hard"
    val questionType: String = "multiple_choice", // "multiple_choice", "true_false", "fill_blank"
    val questionText: String,
    val options: List<String>, // Flexible list of options
    val correctAnswerIndex: Int, // Index of correct answer in options list
    val correctAnswerText: String, // For fill_blank questions
    val explanation: String,
    val hints: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val points: Int = 1,
    val timeLimit: Int? = null, // seconds for this specific question
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String? = null, // AI or user ID
    val source: String? = null // textbook, AI-generated, etc.
)