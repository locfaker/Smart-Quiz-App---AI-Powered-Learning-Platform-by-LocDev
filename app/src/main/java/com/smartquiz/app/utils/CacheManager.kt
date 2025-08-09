package com.smartquiz.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartquiz.app.config.AppConfig
import com.smartquiz.app.data.entities.Question

class CacheManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("smart_quiz_cache", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_QUESTIONS_CACHE = "questions_cache_"
        private const val KEY_CACHE_TIMESTAMP = "cache_timestamp_"
        private const val KEY_AI_GENERATED_QUESTIONS = "ai_questions_"
        private const val KEY_USER_PREFERENCES = "user_preferences"
    }
    
    // Cache questions by subject and difficulty
    fun cacheQuestions(subject: String, difficulty: String, questions: List<Question>) {
        val key = "${KEY_QUESTIONS_CACHE}${subject}_$difficulty"
        val timestampKey = "${KEY_CACHE_TIMESTAMP}${subject}_$difficulty"
        
        val questionsJson = gson.toJson(questions)
        val currentTime = System.currentTimeMillis()
        
        sharedPreferences.edit()
            .putString(key, questionsJson)
            .putLong(timestampKey, currentTime)
            .apply()
    }
    
    // Get cached questions if not expired
    fun getCachedQuestions(subject: String, difficulty: String): List<Question>? {
        val key = "${KEY_QUESTIONS_CACHE}${subject}_$difficulty"
        val timestampKey = "${KEY_CACHE_TIMESTAMP}${subject}_$difficulty"
        
        val cachedTime = sharedPreferences.getLong(timestampKey, 0)
        val currentTime = System.currentTimeMillis()
        
        // Check if cache is expired
        if (currentTime - cachedTime > AppConfig.CACHE_EXPIRY_TIME) {
            clearCache(subject, difficulty)
            return null
        }
        
        val questionsJson = sharedPreferences.getString(key, null) ?: return null
        
        return try {
            val type = object : TypeToken<List<Question>>() {}.type
            gson.fromJson(questionsJson, type)
        } catch (e: Exception) {
            null
        }
    }
    
    // Cache AI generated questions separately
    fun cacheAIQuestions(subject: String, difficulty: String, questions: List<Question>) {
        val key = "${KEY_AI_GENERATED_QUESTIONS}${subject}_$difficulty"
        val questionsJson = gson.toJson(questions)
        
        sharedPreferences.edit()
            .putString(key, questionsJson)
            .putLong("${key}_timestamp", System.currentTimeMillis())
            .apply()
    }
    
    // Get AI generated questions
    fun getAICachedQuestions(subject: String, difficulty: String): List<Question>? {
        val key = "${KEY_AI_GENERATED_QUESTIONS}${subject}_$difficulty"
        val questionsJson = sharedPreferences.getString(key, null) ?: return null
        
        return try {
            val type = object : TypeToken<List<Question>>() {}.type
            gson.fromJson(questionsJson, type)
        } catch (e: Exception) {
            null
        }
    }
    
    // Clear specific cache
    fun clearCache(subject: String, difficulty: String) {
        val key = "${KEY_QUESTIONS_CACHE}${subject}_$difficulty"
        val timestampKey = "${KEY_CACHE_TIMESTAMP}${subject}_$difficulty"
        
        sharedPreferences.edit()
            .remove(key)
            .remove(timestampKey)
            .apply()
    }
    
    // Clear all cache
    fun clearAllCache() {
        val editor = sharedPreferences.edit()
        val allKeys = sharedPreferences.all.keys
        
        allKeys.forEach { key ->
            if (key.startsWith(KEY_QUESTIONS_CACHE) || 
                key.startsWith(KEY_CACHE_TIMESTAMP) ||
                key.startsWith(KEY_AI_GENERATED_QUESTIONS)) {
                editor.remove(key)
            }
        }
        
        editor.apply()
    }
    
    // User preferences
    fun saveUserPreference(key: String, value: Any) {
        val preferences = getUserPreferences().toMutableMap()
        preferences[key] = value
        
        val preferencesJson = gson.toJson(preferences)
        sharedPreferences.edit()
            .putString(KEY_USER_PREFERENCES, preferencesJson)
            .apply()
    }
    
    fun getUserPreferences(): Map<String, Any> {
        val preferencesJson = sharedPreferences.getString(KEY_USER_PREFERENCES, null)
            ?: return emptyMap()
        
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(preferencesJson, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    fun getUserPreference(key: String, defaultValue: Any): Any {
        return getUserPreferences()[key] ?: defaultValue
    }
    
    // Cache statistics
    fun getCacheSize(): Long {
        var totalSize = 0L
        val allKeys = sharedPreferences.all.keys
        
        allKeys.forEach { key ->
            val value = sharedPreferences.getString(key, "")
            totalSize += value?.length?.toLong() ?: 0L
        }
        
        return totalSize
    }
    
    fun getCacheInfo(): Map<String, Any> {
        val allKeys = sharedPreferences.all.keys
        val cacheInfo = mutableMapOf<String, Any>()
        
        var questionCacheCount = 0
        var aiCacheCount = 0
        
        allKeys.forEach { key ->
            when {
                key.startsWith(KEY_QUESTIONS_CACHE) -> questionCacheCount++
                key.startsWith(KEY_AI_GENERATED_QUESTIONS) -> aiCacheCount++
            }
        }
        
        cacheInfo["total_size"] = getCacheSize()
        cacheInfo["question_cache_count"] = questionCacheCount
        cacheInfo["ai_cache_count"] = aiCacheCount
        cacheInfo["last_updated"] = System.currentTimeMillis()
        
        return cacheInfo
    }
}
