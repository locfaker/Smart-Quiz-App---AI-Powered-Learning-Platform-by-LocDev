package com.smartquiz.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartquiz.app.config.AppConfig
import com.smartquiz.app.data.database.AppDatabase
import com.smartquiz.app.databinding.ActivitySplashBinding
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide status bar for immersive experience
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()

        startAnimations()
        initializeApp()
    }

    private fun startAnimations() {
        // Logo animation
        val logoFadeIn = ObjectAnimator.ofFloat(binding.logoImageView, "alpha", 0f, 1f)
        val logoScaleX = ObjectAnimator.ofFloat(binding.logoImageView, "scaleX", 0.5f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.logoImageView, "scaleY", 0.5f, 1f)

        val logoAnimatorSet = AnimatorSet().apply {
            playTogether(logoFadeIn, logoScaleX, logoScaleY)
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        // App name animation
        val appNameFadeIn = ObjectAnimator.ofFloat(binding.appNameTextView, "alpha", 0f, 1f)
        val appNameTranslateY = ObjectAnimator.ofFloat(binding.appNameTextView, "translationY", 50f, 0f)

        val appNameAnimatorSet = AnimatorSet().apply {
            playTogether(appNameFadeIn, appNameTranslateY)
            duration = 600
            startDelay = 400
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Subtitle animation
        val subtitleFadeIn = ObjectAnimator.ofFloat(binding.subtitleTextView, "alpha", 0f, 1f)
        val subtitleTranslateY = ObjectAnimator.ofFloat(binding.subtitleTextView, "translationY", 30f, 0f)

        val subtitleAnimatorSet = AnimatorSet().apply {
            playTogether(subtitleFadeIn, subtitleTranslateY)
            duration = 500
            startDelay = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Loading animation
        val loadingFadeIn = ObjectAnimator.ofFloat(binding.loadingProgressBar, "alpha", 0f, 1f)
        val loadingTextFadeIn = ObjectAnimator.ofFloat(binding.loadingTextView, "alpha", 0f, 1f)

        val loadingAnimatorSet = AnimatorSet().apply {
            playTogether(loadingFadeIn, loadingTextFadeIn)
            duration = 400
            startDelay = 1200
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Start all animations
        logoAnimatorSet.start()
        appNameAnimatorSet.start()
        subtitleAnimatorSet.start()
        loadingAnimatorSet.start()
    }

    private fun initializeApp() {
        lifecycleScope.launch {
            try {
                // Initialize database
                val database = AppDatabase.getDatabase(this@SplashActivity)
                
                // Check if sample data exists, if not populate it
                val questionCount = database.questionDao().getQuestionCountBySubject("Toán học")
                if (questionCount == 0) {
                    updateLoadingText("Đang tải dữ liệu mẫu...")
                    // Database callback will handle sample data population
                }
                
                updateLoadingText("Kiểm tra cấu hình AI...")
                
                // Check AI configuration
                if (!AppConfig.isGeminiApiKeyConfigured()) {
                    updateLoadingText("Chế độ offline - Sử dụng dữ liệu mẫu")
                } else {
                    updateLoadingText("AI đã sẵn sàng!")
                }
                
                updateLoadingText("Hoàn tất!")
                
            } catch (e: Exception) {
                updateLoadingText("Đã xảy ra lỗi: ${e.message}")
            }
            
            // Navigate to main activity after splash duration
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToMain()
            }, splashDuration)
        }
    }

    private fun updateLoadingText(text: String) {
        runOnUiThread {
            binding.loadingTextView.text = text
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        
        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
