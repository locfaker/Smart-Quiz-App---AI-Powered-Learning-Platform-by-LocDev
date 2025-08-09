package com.smartquiz.app.data.api

import com.smartquiz.app.data.models.GenerateFeedbackRequest
import com.smartquiz.app.data.models.GenerateFeedbackResponse
import com.smartquiz.app.data.models.GenerateQuestionsRequest
import com.smartquiz.app.data.models.GenerateQuestionsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface QuizApiService {

    @POST("api/generate-questions")
    suspend fun generateQuestions(
        @Body request: GenerateQuestionsRequest
    ): Response<GenerateQuestionsResponse>

    @POST("api/generate-feedback")
    suspend fun generateFeedback(
        @Body request: GenerateFeedbackRequest
    ): Response<GenerateFeedbackResponse>
}
