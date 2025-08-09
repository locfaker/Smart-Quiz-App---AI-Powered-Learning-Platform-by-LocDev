package com.smartquiz.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartquiz.app.data.SampleDataProvider
import com.smartquiz.app.data.dao.QuestionDao
import com.smartquiz.app.data.dao.QuizAnswerDao
import com.smartquiz.app.data.dao.QuizDao
import com.smartquiz.app.data.dao.StudySessionDao
import com.smartquiz.app.data.dao.UserProfileDao
import com.smartquiz.app.data.entities.Question
import com.smartquiz.app.data.entities.Quiz
import com.smartquiz.app.data.entities.QuizAnswer
import com.smartquiz.app.data.entities.StudySession
import com.smartquiz.app.data.entities.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Quiz::class, 
        Question::class, 
        QuizAnswer::class,
        UserProfile::class,
        StudySession::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizAnswerDao(): QuizAnswerDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_profiles` (
                        `id` TEXT NOT NULL,
                        `username` TEXT NOT NULL,
                        `email` TEXT,
                        `displayName` TEXT NOT NULL,
                        `avatarUrl` TEXT,
                        `level` INTEGER NOT NULL,
                        `totalXp` INTEGER NOT NULL,
                        `currentStreak` INTEGER NOT NULL,
                        `longestStreak` INTEGER NOT NULL,
                        `totalQuizzes` INTEGER NOT NULL,
                        `totalCorrectAnswers` INTEGER NOT NULL,
                        `totalTimeSpent` INTEGER NOT NULL,
                        `favoriteSubjects` TEXT NOT NULL,
                        `achievements` TEXT NOT NULL,
                        `preferredDifficulty` TEXT NOT NULL,
                        `dailyGoal` INTEGER NOT NULL,
                        `lastActiveAt` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_sessions` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `difficulty` TEXT NOT NULL,
                        `sessionType` TEXT NOT NULL,
                        `questionsAttempted` INTEGER NOT NULL,
                        `questionsCorrect` INTEGER NOT NULL,
                        `totalTimeSpent` INTEGER NOT NULL,
                        `xpEarned` INTEGER NOT NULL,
                        `streakBonus` INTEGER NOT NULL,
                        `completedQuizIds` TEXT NOT NULL,
                        `startedAt` INTEGER NOT NULL,
                        `endedAt` INTEGER,
                        `isCompleted` INTEGER NOT NULL,
                        `notes` TEXT,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Create indices
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_profiles_email` ON `user_profiles` (`email`)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_profiles_username` ON `user_profiles` (`username`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_study_sessions_userId` ON `study_sessions` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_study_sessions_startedAt` ON `study_sessions` (`startedAt`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_study_sessions_subject` ON `study_sessions` (`subject`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_quiz_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.questionDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(questionDao: QuestionDao) {
            // Insert sample questions
            val sampleQuestions = SampleDataProvider.getAllSampleQuestions()
            questionDao.insertQuestions(sampleQuestions)
        }
    }
}
