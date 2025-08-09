package com.smartquiz.app.di

import com.smartquiz.app.data.repository.QuestionRepository
import com.smartquiz.app.data.repository.QuizAnswerRepository
import com.smartquiz.app.data.repository.QuizRepository
import com.smartquiz.app.data.repository.impl.QuestionRepositoryImpl
import com.smartquiz.app.data.repository.impl.QuizAnswerRepositoryImpl
import com.smartquiz.app.data.repository.impl.QuizRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        quizRepositoryImpl: QuizRepositoryImpl
    ): QuizRepository

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        questionRepositoryImpl: QuestionRepositoryImpl
    ): QuestionRepository

    @Binds
    @Singleton
    abstract fun bindQuizAnswerRepository(
        quizAnswerRepositoryImpl: QuizAnswerRepositoryImpl
    ): QuizAnswerRepository
}