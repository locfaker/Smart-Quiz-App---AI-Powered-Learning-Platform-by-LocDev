package com.smartquiz.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartquiz.app.databinding.ActivitySubjectListBinding
import com.smartquiz.app.ui.adapter.SubjectDetailAdapter
import com.smartquiz.app.ui.viewmodel.SubjectListViewModel

class SubjectListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectListBinding
    private lateinit var viewModel: SubjectListViewModel
    private lateinit var subjectAdapter: SubjectDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SubjectListViewModel::class.java]
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectDetailAdapter { subjectInfo ->
            showDifficultyDialog(subjectInfo.name)
        }
        
        binding.recyclerViewSubjects.apply {
            layoutManager = LinearLayoutManager(this@SubjectListActivity)
            adapter = subjectAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnGenerateQuestions.setOnClickListener {
            Toast.makeText(this, "Tính năng tạo câu hỏi sẽ được cập nhật sau", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.subjects.observe(this) { subjects ->
            subjectAdapter.submitList(subjects)
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

    private fun showDifficultyDialog(subject: String) {
        val difficulties = arrayOf("Dễ", "Trung bình", "Khó")
        val difficultyValues = arrayOf("easy", "medium", "hard")
        
        AlertDialog.Builder(this)
            .setTitle("Chọn độ khó")
            .setItems(difficulties) { _, which ->
                showQuestionCountDialog(subject, difficultyValues[which])
            }
            .show()
    }

    private fun showQuestionCountDialog(subject: String, difficulty: String) {
        val counts = arrayOf("5 câu", "10 câu", "15 câu", "20 câu")
        val countValues = arrayOf(5, 10, 15, 20)
        
        AlertDialog.Builder(this)
            .setTitle("Chọn số câu hỏi")
            .setItems(counts) { _, which ->
                startQuiz(subject, difficulty, countValues[which])
            }
            .show()
    }

    private fun startQuiz(subject: String, difficulty: String, questionCount: Int) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("subject", subject)
        intent.putExtra("difficulty", difficulty)
        intent.putExtra("question_count", questionCount)
        startActivity(intent)
    }
}
