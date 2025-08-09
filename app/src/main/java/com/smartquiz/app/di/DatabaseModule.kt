package com.smartquiz.app.di

import android.content.Context
import androidx.room.Room
import com.smartquiz.app.data.dao.QuestionDao
import com.smartquiz.app.data.dao.QuizAnswerDao
import com.smartquiz.app.data.dao.QuizDao
import com.smartquiz.app.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "smart_quiz_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideQuizDao(database: AppDatabase): QuizDao = database.quizDao()

    @Provides
    fun provideQuestionDao(database: AppDatabase): QuestionDao = database.questionDao()

    @Provides
    fun provideQuizAnswerDao(database: AppDatabase): QuizAnswerDao = database.quizAnswerDao()
}