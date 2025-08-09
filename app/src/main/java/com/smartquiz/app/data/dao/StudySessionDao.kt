package com.smartquiz.app.data.dao

import androidx.room.*
import com.smartquiz.app.data.entities.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getStudySessionsByUser(userId: String): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND isCompleted = 0 LIMIT 1")
    suspend fun getCurrentSession(userId: String): StudySession?

    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun getStudySessionById(sessionId: String): StudySession?

    @Query("""
        SELECT * FROM study_sessions 
        WHERE userId = :userId 
        AND subject = :subject 
        ORDER BY startedAt DESC 
        LIMIT :limit
    """)
    fun getStudySessionsBySubject(userId: String, subject: String, limit: Int = 10): Flow<List<StudySession>>

    @Query("""
        SELECT * FROM study_sessions 
        WHERE userId = :userId 
        AND startedAt >= :fromTimestamp 
        AND startedAt <= :toTimestamp 
        ORDER BY startedAt DESC
    """)
    fun getStudySessionsByDateRange(
        userId: String, 
        fromTimestamp: Long, 
        toTimestamp: Long
    ): Flow<List<StudySession>>

    @Query("""
        SELECT COUNT(*) FROM study_sessions 
        WHERE userId = :userId 
        AND startedAt >= :todayStart 
        AND startedAt < :todayEnd
    """)
    suspend fun getTodaySessionCount(userId: String, todayStart: Long, todayEnd: Long): Int

    @Query("""
        SELECT SUM(questionsAttempted) FROM study_sessions 
        WHERE userId = :userId 
        AND startedAt >= :todayStart 
        AND startedAt < :todayEnd
    """)
    suspend fun getTodayQuestionsCount(userId: String, todayStart: Long, todayEnd: Long): Int?

    @Query("""
        SELECT AVG(CAST(questionsCorrect AS FLOAT) / questionsAttempted * 100) 
        FROM study_sessions 
        WHERE userId = :userId 
        AND questionsAttempted > 0
        AND startedAt >= :fromTimestamp
    """)
    suspend fun getAverageAccuracy(userId: String, fromTimestamp: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(studySession: StudySession)

    @Update
    suspend fun updateStudySession(studySession: StudySession)

    @Query("UPDATE study_sessions SET isCompleted = 1, endedAt = :endTime WHERE id = :sessionId")
    suspend fun completeSession(sessionId: String, endTime: Long)

    @Query("UPDATE study_sessions SET questionsAttempted = :attempted, questionsCorrect = :correct WHERE id = :sessionId")
    suspend fun updateSessionProgress(sessionId: String, attempted: Int, correct: Int)

    @Query("UPDATE study_sessions SET totalTimeSpent = :timeMs WHERE id = :sessionId")
    suspend fun updateSessionTime(sessionId: String, timeMs: Long)

    @Query("DELETE FROM study_sessions WHERE id = :sessionId")
    suspend fun deleteStudySession(sessionId: String)

    @Query("DELETE FROM study_sessions WHERE userId = :userId AND startedAt < :cutoffTimestamp")
    suspend fun deleteOldSessions(userId: String, cutoffTimestamp: Long)
}