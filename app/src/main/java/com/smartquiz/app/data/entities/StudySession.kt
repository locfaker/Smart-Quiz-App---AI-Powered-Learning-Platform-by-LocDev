package com.smartquiz.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.smartquiz.app.data.database.Converters
import java.util.UUID

@Entity(
    tableName = "study_sessions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["startedAt"]),
        Index(value = ["subject"])
    ]
)
@TypeConverters(Converters::class)
data class StudySession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val subject: String,
    val difficulty: String,
    val sessionType: String, // "practice", "test", "review"
    val questionsAttempted: Int = 0,
    val questionsCorrect: Int = 0,
    val totalTimeSpent: Long = 0, // in milliseconds
    val xpEarned: Int = 0,
    val streakBonus: Int = 0,
    val completedQuizIds: List<String> = emptyList(),
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isCompleted: Boolean = false,
    val notes: String? = null
)