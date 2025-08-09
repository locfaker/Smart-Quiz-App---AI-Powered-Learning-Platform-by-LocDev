package com.smartquiz.app.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartquiz.app.config.AppConfig
import com.smartquiz.app.data.entities.Question
import com.smartquiz.app.data.models.WrongAnswer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAIService {

    private val apiKey = AppConfig.GEMINI_API_KEY
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 2048
        }
    )

    private val gson = Gson()

    suspend fun generateQuestions(
        subject: String,
        difficulty: String,
        count: Int
    ): Result<List<Question>> = withContext(Dispatchers.IO) {
        try {
            if (!AppConfig.isGeminiApiKeyConfigured()) {
                return@withContext Result.failure(Exception("Gemini API key chưa được cấu hình. Vui lòng cập nhật API key trong AppConfig.kt"))
            }
            val difficultyText = when (difficulty) {
                "easy" -> "dễ"
                "medium" -> "trung bình"
                "hard" -> "khó"
                else -> difficulty
            }

            val prompt = """
                Tạo $count câu hỏi trắc nghiệm cho môn $subject với độ khó $difficultyText.
                
                Yêu cầu:
                - Câu hỏi phải chính xác và phù hợp với chương trình học phổ thông Việt Nam
                - Mỗi câu có 4 đáp án A, B, C, D
                - Chỉ có 1 đáp án đúng
                - Có giải thích chi tiết cho đáp án đúng
                - Trả về định dạng JSON array với cấu trúc:
                
                [
                  {
                    "questionText": "Nội dung câu hỏi",
                    "optionA": "Đáp án A",
                    "optionB": "Đáp án B", 
                    "optionC": "Đáp án C",
                    "optionD": "Đáp án D",
                    "correctAnswer": "A",
                    "explanation": "Giải thích chi tiết"
                  }
                ]
                
                Chỉ trả về JSON, không có text khác.
            """.trimIndent()

            val response = generativeModel.generateContent(
                content {
                    text(prompt)
                }
            )

            val jsonResponse = response.text ?: throw Exception("Empty response from AI")
            
            // Parse JSON response
            val listType = object : TypeToken<List<QuestionResponse>>() {}.type
            val questionResponses: List<QuestionResponse> = gson.fromJson(jsonResponse, listType)
            
            val questions = questionResponses.map { questionResponse ->
                Question(
                    subject = subject,
                    difficulty = difficulty,
                    questionText = questionResponse.questionText,
                    optionA = questionResponse.optionA,
                    optionB = questionResponse.optionB,
                    optionC = questionResponse.optionC,
                    optionD = questionResponse.optionD,
                    correctAnswer = questionResponse.correctAnswer,
                    explanation = questionResponse.explanation
                )
            }
            
            Result.success(questions)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateFeedback(
        subject: String,
        difficulty: String,
        totalQuestions: Int,
        correctAnswers: Int,
        timeSpent: Long,
        wrongAnswers: List<WrongAnswer>
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            if (!AppConfig.isGeminiApiKeyConfigured()) {
                return@withContext Result.failure(Exception("Gemini API key chưa được cấu hình"))
            }
            val score = (correctAnswers.toDouble() / totalQuestions * 100).toInt()
            val timeMinutes = timeSpent / 60000
            val timeSeconds = (timeSpent % 60000) / 1000
            
            val difficultyText = when (difficulty) {
                "easy" -> "dễ"
                "medium" -> "trung bình" 
                "hard" -> "khó"
                else -> difficulty
            }

            val wrongAnswersText = if (wrongAnswers.isNotEmpty()) {
                wrongAnswers.joinToString("\n") { 
                    "- Câu: ${it.question}\n  Đáp án đúng: ${it.correctAnswer}, Bạn chọn: ${it.userAnswer}"
                }
            } else {
                "Bạn đã trả lời đúng tất cả câu hỏi!"
            }

            val prompt = """
                Phân tích kết quả quiz và đưa ra feedback chi tiết:
                
                Thông tin:
                - Môn học: $subject
                - Độ khó: $difficultyText
                - Điểm số: $score% ($correctAnswers/$totalQuestions câu đúng)
                - Thời gian: ${timeMinutes}m${timeSeconds}s
                
                Câu trả lời sai:
                $wrongAnswersText
                
                Hãy tạo 2 phần:
                1. FEEDBACK: Nhận xét tích cực, động viên và đánh giá kết quả (2-3 câu)
                2. SUGGESTIONS: Gợi ý cụ thể để cải thiện (3-4 gợi ý ngắn gọn)
                
                Trả về định dạng JSON:
                {
                  "feedback": "Nhận xét tích cực...",
                  "suggestions": "• Gợi ý 1\n• Gợi ý 2\n• Gợi ý 3"
                }
                
                Chỉ trả về JSON, không có text khác.
            """.trimIndent()

            val response = generativeModel.generateContent(
                content {
                    text(prompt)
                }
            )

            val jsonResponse = response.text ?: throw Exception("Empty response from AI")
            
            val feedbackResponse: FeedbackResponse = gson.fromJson(jsonResponse, FeedbackResponse::class.java)
            
            Result.success(Pair(feedbackResponse.feedback, feedbackResponse.suggestions))
            
        } catch (e: Exception) {
            // Fallback feedback if AI fails
            val score = (correctAnswers.toDouble() / totalQuestions * 100).toInt()
            val fallbackFeedback = when {
                score >= 90 -> "Xuất sắc! Bạn đã thể hiện sự hiểu biết vững vàng về $subject."
                score >= 80 -> "Rất tốt! Bạn đã nắm được phần lớn kiến thức về $subject."
                score >= 70 -> "Khá tốt! Bạn đã có nền tảng tốt về $subject."
                score >= 60 -> "Ổn! Bạn cần ôn tập thêm một số phần trong $subject."
                else -> "Cần cố gắng hơn! Hãy dành thời gian ôn tập kỹ lưỡng $subject."
            }
            
            val fallbackSuggestions = """
                • Ôn tập lại các khái niệm cơ bản
                • Làm thêm bài tập thực hành
                • Tìm hiểu sâu hơn về các chủ đề còn yếu
                • Luyện tập thường xuyên để ghi nhớ lâu hơn
            """.trimIndent()
            
            Result.success(Pair(fallbackFeedback, fallbackSuggestions))
        }
    }

    data class QuestionResponse(
        val questionText: String,
        val optionA: String,
        val optionB: String,
        val optionC: String,
        val optionD: String,
        val correctAnswer: String,
        val explanation: String
    )

    data class FeedbackResponse(
        val feedback: String,
        val suggestions: String
    )
}
