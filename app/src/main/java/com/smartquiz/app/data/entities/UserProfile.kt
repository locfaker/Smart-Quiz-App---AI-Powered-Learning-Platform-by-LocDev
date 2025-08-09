package com.smartquiz.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.smartquiz.app.data.database.Converters
import java.util.UUID

@Entity(
    tableName = "user_profiles",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
@TypeConverters(Converters::class)
data class UserProfile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String? = null,
    val displayName: String,
    val avatarUrl: String? = null,
    val level: Int = 1,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalQuizzes: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalTimeSpent: Long = 0, // in milliseconds
    val favoriteSubjects: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val preferredDifficulty: String = "medium",
    val dailyGoal: Int = 5, // questions per day
    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)