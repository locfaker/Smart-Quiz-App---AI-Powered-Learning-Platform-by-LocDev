package com.smartquiz.app.utils

object Constants {
    
    // Database
    const val DATABASE_NAME = "smart_quiz_database"
    const val DATABASE_VERSION = 1
    
    // API
    const val BASE_URL = "https://api.smartquiz.com/"
    const val API_TIMEOUT = 30L
    
    // SharedPreferences
    const val PREFS_NAME = "smart_quiz_prefs"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_USER_NAME = "user_name"
    const val KEY_SOUND_ENABLED = "sound_enabled"
    const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    const val KEY_DARK_MODE = "dark_mode"
    
    // Quiz Settings
    const val DEFAULT_QUIZ_TIME_MINUTES = 15
    const val MIN_QUIZ_TIME_MINUTES = 5
    const val MAX_QUIZ_TIME_MINUTES = 60
    const val DEFAULT_QUESTIONS_PER_QUIZ = 10
    const val MIN_QUESTIONS_PER_QUIZ = 5
    const val MAX_QUESTIONS_PER_QUIZ = 50
    
    // Difficulty Levels
    const val DIFFICULTY_EASY = "easy"
    const val DIFFICULTY_MEDIUM = "medium"
    const val DIFFICULTY_HARD = "hard"
    
    // Subjects
    const val SUBJECT_MATH = "math"
    const val SUBJECT_PHYSICS = "physics"
    const val SUBJECT_CHEMISTRY = "chemistry"
    const val SUBJECT_BIOLOGY = "biology"
    const val SUBJECT_HISTORY = "history"
    const val SUBJECT_GEOGRAPHY = "geography"
    const val SUBJECT_LITERATURE = "literature"
    const val SUBJECT_ENGLISH = "english"
    
    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L
    
    // Intent Extras
    const val EXTRA_QUIZ_ID = "quiz_id"
    const val EXTRA_SUBJECT = "subject"
    const val EXTRA_DIFFICULTY = "difficulty"
    const val EXTRA_QUESTION_COUNT = "question_count"
    const val EXTRA_TIME_LIMIT = "time_limit"
    const val EXTRA_SCORE = "score"
    const val EXTRA_TOTAL_QUESTIONS = "total_questions"
    const val EXTRA_CORRECT_ANSWERS = "correct_answers"
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "smart_quiz_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Smart Quiz Notifications"
    const val NOTIFICATION_ID_REMINDER = 1001
    
    // WorkManager
    const val WORK_NAME_DAILY_REMINDER = "daily_reminder_work"
    const val WORK_NAME_SYNC_DATA = "sync_data_work"
}