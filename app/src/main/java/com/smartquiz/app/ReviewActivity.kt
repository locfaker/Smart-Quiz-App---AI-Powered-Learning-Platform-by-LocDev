package com.smartquiz.app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.smartquiz.app.databinding.ActivityReviewBinding
import com.smartquiz.app.ui.viewmodel.ReviewViewModel

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var viewModel: ReviewViewModel
    private var isAnswerShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupClickListeners()
        observeViewModel()
        startReview()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ReviewViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.radioGroupAnswers.setOnCheckedChangeListener { _, checkedId ->
            val selectedAnswer = when (checkedId) {
                R.id.radioOptionA -> "A"
                R.id.radioOptionB -> "B"
                R.id.radioOptionC -> "C"
                R.id.radioOptionD -> "D"
                else -> ""
            }
            
            if (selectedAnswer.isNotEmpty()) {
                viewModel.selectAnswer(selectedAnswer)
            }
        }

        binding.btnShowAnswer.setOnClickListener {
            if (!isAnswerShown) {
                showCorrectAnswer()
                isAnswerShown = true
                binding.btnShowAnswer.text = "Ẩn đáp án"
            } else {
                hideAnswer()
                isAnswerShown = false
                binding.btnShowAnswer.text = "Hiện đáp án"
            }
        }

        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
            resetAnswerDisplay()
        }
    }

    private fun observeViewModel() {
        viewModel.currentQuestion.observe(this) { question ->
            question?.let {
                binding.textQuestion.text = it.questionText
                binding.radioOptionA.text = "A. ${it.optionA}"
                binding.radioOptionB.text = "B. ${it.optionB}"
                binding.radioOptionC.text = "C. ${it.optionC}"
                binding.radioOptionD.text = "D. ${it.optionD}"
                binding.textExplanation.text = it.explanation
            }
        }

        viewModel.currentQuestionIndex.observe(this) { index ->
            val total = viewModel.getTotalQuestions()
            binding.textQuestionNumber.text = "Câu ${index + 1}/$total"
            
            val progress = ((index + 1).toFloat() / total * 100).toInt()
            binding.progressBarReview.progress = progress
            
            // Update next button text for last question
            if (index == total - 1) {
                binding.btnNext.text = "Hoàn thành"
            } else {
                binding.btnNext.text = "Tiếp theo"
            }
        }

        viewModel.isCompleted.observe(this) { isCompleted ->
            if (isCompleted) {
                Toast.makeText(this, "Đã hoàn thành ôn tập!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun startReview() {
        val subject = intent.getStringExtra("subject")
        val difficulty = intent.getStringExtra("difficulty")
        
        if (subject != null) {
            viewModel.startReview(subject, difficulty)
        } else {
            Toast.makeText(this, "Không có dữ liệu để ôn tập", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showCorrectAnswer() {
        val correctAnswer = viewModel.getCurrentCorrectAnswer()
        val userAnswer = viewModel.getCurrentUserAnswer()
        
        // Reset all card colors
        resetCardColors()
        
        // Highlight correct answer in green
        val correctCard = when (correctAnswer) {
            "A" -> binding.cardOptionA
            "B" -> binding.cardOptionB
            "C" -> binding.cardOptionC
            "D" -> binding.cardOptionD
            else -> null
        }
        correctCard?.setCardBackgroundColor(Color.parseColor("#4CAF50"))
        
        // Highlight wrong answer in red if user selected wrong
        if (userAnswer.isNotEmpty() && userAnswer != correctAnswer) {
            val wrongCard = when (userAnswer) {
                "A" -> binding.cardOptionA
                "B" -> binding.cardOptionB
                "C" -> binding.cardOptionC
                "D" -> binding.cardOptionD
                else -> null
            }
            wrongCard?.setCardBackgroundColor(Color.parseColor("#F44336"))
        }
        
        // Show explanation
        binding.cardExplanation.visibility = View.VISIBLE
    }

    private fun hideAnswer() {
        resetCardColors()
        binding.cardExplanation.visibility = View.GONE
    }

    private fun resetCardColors() {
        val defaultColor = Color.parseColor("#FFFFFF")
        binding.cardOptionA.setCardBackgroundColor(defaultColor)
        binding.cardOptionB.setCardBackgroundColor(defaultColor)
        binding.cardOptionC.setCardBackgroundColor(defaultColor)
        binding.cardOptionD.setCardBackgroundColor(defaultColor)
    }

    private fun resetAnswerDisplay() {
        binding.radioGroupAnswers.clearCheck()
        resetCardColors()
        binding.cardExplanation.visibility = View.GONE
        isAnswerShown = false
        binding.btnShowAnswer.text = "Hiện đáp án"
    }
}
