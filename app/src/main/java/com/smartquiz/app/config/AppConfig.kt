package com.smartquiz.app.config

object AppConfig {
    
    // Gemini AI Configuration
    // Để lấy API key miễn phí:
    // 1. Truy cập https://makersuite.google.com/app/apikey
    // 2. Đăng nhập với Google account
    // 3. Tạo API key mới
    // 4. Thay thế giá trị dưới đây
    const val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
    
    // App Settings
    const val DEFAULT_QUIZ_TIME_LIMIT = 30 * 60 * 1000L // 30 minutes
    const val DEFAULT_QUESTION_COUNT = 10
    const val MIN_QUESTION_COUNT = 5
    const val MAX_QUESTION_COUNT = 50
    
    // Database Settings
    const val DATABASE_NAME = "smart_quiz_database"
    const val DATABASE_VERSION = 1
    
    // UI Settings
    const val ANIMATION_DURATION = 300L
    const val SHIMMER_DURATION = 1000L
    
    // Cache Settings
    const val CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000L // 24 hours
    
    // Validation
    fun isGeminiApiKeyConfigured(): Boolean {
        return GEMINI_API_KEY != "YOUR_GEMINI_API_KEY_HERE" && GEMINI_API_KEY.isNotBlank()
    }
}
