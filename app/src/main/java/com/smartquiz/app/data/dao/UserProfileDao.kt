package com.smartquiz.app.data.dao

import androidx.room.*
import com.smartquiz.app.data.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    fun getCurrentUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    suspend fun getUserProfileById(userId: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE username = :username")
    suspend fun getUserProfileByUsername(username: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE email = :email")
    suspend fun getUserProfileByEmail(email: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Query("UPDATE user_profiles SET totalXp = totalXp + :xp WHERE id = :userId")
    suspend fun addXp(userId: String, xp: Int)

    @Query("UPDATE user_profiles SET currentStreak = :streak WHERE id = :userId")
    suspend fun updateStreak(userId: String, streak: Int)

    @Query("UPDATE user_profiles SET longestStreak = :streak WHERE id = :userId AND longestStreak < :streak")
    suspend fun updateLongestStreak(userId: String, streak: Int)

    @Query("UPDATE user_profiles SET totalQuizzes = totalQuizzes + 1 WHERE id = :userId")
    suspend fun incrementTotalQuizzes(userId: String)

    @Query("UPDATE user_profiles SET totalCorrectAnswers = totalCorrectAnswers + :count WHERE id = :userId")
    suspend fun addCorrectAnswers(userId: String, count: Int)

    @Query("UPDATE user_profiles SET totalTimeSpent = totalTimeSpent + :timeMs WHERE id = :userId")
    suspend fun addTimeSpent(userId: String, timeMs: Long)

    @Query("UPDATE user_profiles SET lastActiveAt = :timestamp WHERE id = :userId")
    suspend fun updateLastActiveAt(userId: String, timestamp: Long)

    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun deleteUserProfile(userId: String)

    @Query("SELECT COUNT(*) FROM user_profiles WHERE isActive = 1")
    suspend fun getActiveUserCount(): Int
}