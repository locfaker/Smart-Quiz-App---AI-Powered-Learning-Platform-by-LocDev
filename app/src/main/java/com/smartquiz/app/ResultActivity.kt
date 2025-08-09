package com.smartquiz.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.smartquiz.app.databinding.ActivityResultBinding
import com.smartquiz.app.ui.viewmodel.ResultViewModel

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var viewModel: ResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupClickListeners()
        observeViewModel()
        loadQuizResult()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ResultViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.btnRetryQuiz.setOnClickListener {
            // Get quiz info and restart with same parameters
            viewModel.quizResult.value?.let { result ->
                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("subject", result.quiz.subject)
                intent.putExtra("difficulty", result.quiz.difficulty)
                intent.putExtra("question_count", result.quiz.totalQuestions)
                startActivity(intent)
                finish()
            }
        }

        binding.btnBackToHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.quizResult.observe(this) { result ->
            result?.let {
                updateUI(it)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateUI(result: ResultViewModel.QuizResult) {
        val quiz = result.quiz
        
        // Score
        binding.textScore.text = "${quiz.score.toInt()}%"
        binding.textScoreDescription.text = getString(
            R.string.your_score,
            quiz.correctAnswers,
            quiz.totalQuestions
        )
        
        // Stats
        binding.textTimeSpent.text = viewModel.getTimeSpentFormatted()
        binding.textCorrectAnswers.text = viewModel.getCorrectAnswersText()
        
        // AI Feedback
        binding.textAiFeedback.text = quiz.aiFeedback
        
        // Suggestions
        binding.textSuggestions.text = quiz.suggestions
        
        // Set score color based on performance
        val scoreColor = when {
            quiz.score >= 80 -> getColor(android.R.color.holo_green_dark)
            quiz.score >= 60 -> getColor(android.R.color.holo_orange_dark)
            else -> getColor(android.R.color.holo_red_dark)
        }
        binding.textScore.setTextColor(scoreColor)
    }

    private fun loadQuizResult() {
        val quizId = intent.getLongExtra("quiz_id", -1)
        if (quizId != -1L) {
            viewModel.loadQuizResult(quizId)
        } else {
            Toast.makeText(this, "Không tìm thấy kết quả quiz", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
