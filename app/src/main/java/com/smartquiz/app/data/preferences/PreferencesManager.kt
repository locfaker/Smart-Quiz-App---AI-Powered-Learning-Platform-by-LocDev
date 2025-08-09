package com.smartquiz.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartquiz.app.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFS_NAME)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val dataStore = context.dataStore
    
    // Keys
    private object PreferencesKeys {
        val FIRST_LAUNCH = booleanPreferencesKey(Constants.KEY_FIRST_LAUNCH)
        val USER_NAME = stringPreferencesKey(Constants.KEY_USER_NAME)
        val SOUND_ENABLED = booleanPreferencesKey(Constants.KEY_SOUND_ENABLED)
        val VIBRATION_ENABLED = booleanPreferencesKey(Constants.KEY_VIBRATION_ENABLED)
        val DARK_MODE = booleanPreferencesKey(Constants.KEY_DARK_MODE)
        val QUIZ_TIME_MINUTES = intPreferencesKey("quiz_time_minutes")
        val QUESTIONS_PER_QUIZ = intPreferencesKey("questions_per_quiz")
    }
    
    // First Launch
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
    }
    
    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = false
        }
    }
    
    // User Name
    val userName: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }
    
    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }
    
    // Sound Settings
    val isSoundEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SOUND_ENABLED] ?: true
    }
    
    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }
    
    // Vibration Settings
    val isVibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
    }
    
    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }
    
    // Dark Mode
    val isDarkModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_MODE] ?: false
    }
    
    suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }
    
    // Quiz Time
    val quizTimeMinutes: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUIZ_TIME_MINUTES] ?: Constants.DEFAULT_QUIZ_TIME_MINUTES
    }
    
    suspend fun setQuizTimeMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIZ_TIME_MINUTES] = minutes
        }
    }
    
    // Questions Per Quiz
    val questionsPerQuiz: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUESTIONS_PER_QUIZ] ?: Constants.DEFAULT_QUESTIONS_PER_QUIZ
    }
    
    suspend fun setQuestionsPerQuiz(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUESTIONS_PER_QUIZ] = count
        }
    }
    
    // Clear all preferences
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}