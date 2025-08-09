package com.smartquiz.app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.smartquiz.app.databinding.ActivityQuizBinding
import com.smartquiz.app.ui.viewmodel.QuizViewModel

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var viewModel: QuizViewModel
    private var timer: CountDownTimer? = null
    private var timeLimit = 30 * 60 * 1000L // 30 minutes in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupClickListeners()
        observeViewModel()
        startQuizFromIntent()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            showExitConfirmation()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[QuizViewModel::class.java]
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
                viewModel.answerQuestion(selectedAnswer)
                binding.btnNext.isEnabled = true
            }
        }

        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
            binding.radioGroupAnswers.clearCheck()
            binding.btnNext.isEnabled = false
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
            }
        }

        viewModel.quizState.observe(this) { state ->
            if (state.questions.isNotEmpty()) {
                val progress = ((state.currentQuestionIndex + 1).toFloat() / state.questions.size * 100).toInt()
                binding.progressBarQuiz.progress = progress
                
                binding.textQuestionNumber.text = getString(
                    R.string.question_number,
                    state.currentQuestionIndex + 1,
                    state.questions.size
                )
                
                // Update next button text for last question
                if (state.currentQuestionIndex == state.questions.size - 1) {
                    binding.btnNext.text = getString(R.string.finish_quiz)
                } else {
                    binding.btnNext.text = getString(R.string.next_question)
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnNext.isEnabled = !isLoading && binding.radioGroupAnswers.checkedRadioButtonId != -1
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.quizCompleted.observe(this) { quizId ->
            quizId?.let {
                timer?.cancel()
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("quiz_id", it)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun startQuizFromIntent() {
        val subject = intent.getStringExtra("subject") ?: return
        val difficulty = intent.getStringExtra("difficulty") ?: return
        val questionCount = intent.getIntExtra("question_count", 10)
        
        binding.toolbar.title = "$subject - ${getDifficultyText(difficulty)}"
        
        viewModel.startQuiz(subject, difficulty, questionCount)
        startTimer()
    }

    private fun getDifficultyText(difficulty: String): String {
        return when (difficulty) {
            "easy" -> "Dễ"
            "medium" -> "Trung bình"
            "hard" -> "Khó"
            else -> difficulty
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLimit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.textTimer.text = getString(R.string.quiz_timer, String.format("%02d:%02d", minutes, seconds))
            }

            override fun onFinish() {
                Toast.makeText(this@QuizActivity, "Hết thời gian!", Toast.LENGTH_SHORT).show()
                viewModel.nextQuestion() // This will finish the quiz
            }
        }.start()
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Thoát quiz")
            .setMessage("Bạn có chắc chắn muốn thoát? Tiến trình sẽ bị mất.")
            .setPositiveButton("Thoát") { _, _ ->
                timer?.cancel()
                finish()
            }
            .setNegativeButton("Tiếp tục", null)
            .show()
    }

    override fun onBackPressed() {
        showExitConfirmation()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
