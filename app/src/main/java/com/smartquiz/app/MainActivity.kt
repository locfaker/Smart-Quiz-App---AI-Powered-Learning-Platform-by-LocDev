package com.smartquiz.app

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartquiz.app.config.AppConfig
import com.smartquiz.app.databinding.ActivityMainBinding
import com.smartquiz.app.ui.adapter.RecentQuizAdapter
import com.smartquiz.app.ui.adapter.SubjectAdapter
import com.smartquiz.app.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var recentQuizAdapter: RecentQuizAdapter
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
        startEntranceAnimations()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    private fun setupRecyclerViews() {
        // Recent Quizzes RecyclerView
        recentQuizAdapter = RecentQuizAdapter { quiz ->
            // Handle quiz item click - navigate to result
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("quiz_id", quiz.id)
            startActivity(intent)
        }
        
        binding.recyclerViewRecentQuizzes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recentQuizAdapter
        }

        // Subjects RecyclerView
        subjectAdapter = SubjectAdapter { subject ->
            // Handle subject click - navigate to subject list
            val intent = Intent(this, SubjectListActivity::class.java)
            intent.putExtra("selected_subject", subject)
            startActivity(intent)
        }
        
        binding.recyclerViewSubjects.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = subjectAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnStartQuiz.setOnClickListener {
            val intent = Intent(this, SubjectListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.recentQuizzes.observe(this) { quizzes ->
            hideShimmer(binding.shimmerRecentQuizzes, binding.recyclerViewRecentQuizzes)
            recentQuizAdapter.submitList(quizzes)
            animateRecyclerView(binding.recyclerViewRecentQuizzes)
        }

        viewModel.availableSubjects.observe(this) { subjects ->
            hideShimmer(binding.shimmerSubjects, binding.recyclerViewSubjects)
            subjectAdapter.submitList(subjects)
            animateRecyclerView(binding.recyclerViewSubjects)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showShimmer()
            }
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                hideShimmer(binding.shimmerRecentQuizzes, binding.recyclerViewRecentQuizzes)
                hideShimmer(binding.shimmerSubjects, binding.recyclerViewSubjects)
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    private fun startEntranceAnimations() {
        // Animate header elements
        val headerElements = listOf(
            binding.btnStartQuiz
        )

        headerElements.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f

            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = AppConfig.ANIMATION_DURATION
                startDelay = index * 100L
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }

            ObjectAnimator.ofFloat(view, "translationY", 50f, 0f).apply {
                duration = AppConfig.ANIMATION_DURATION
                startDelay = index * 100L
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    private fun showShimmer() {
        binding.shimmerRecentQuizzes.visibility = View.VISIBLE
        binding.shimmerSubjects.visibility = View.VISIBLE
        binding.recyclerViewRecentQuizzes.visibility = View.GONE
        binding.recyclerViewSubjects.visibility = View.GONE

        binding.shimmerRecentQuizzes.startShimmer()
        binding.shimmerSubjects.startShimmer()
    }

    private fun hideShimmer(shimmerView: View, recyclerView: View) {
        shimmerView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        if (shimmerView is com.facebook.shimmer.ShimmerFrameLayout) {
            shimmerView.stopShimmer()
        }
    }

    private fun animateRecyclerView(recyclerView: View) {
        recyclerView.alpha = 0f
        recyclerView.translationY = 30f

        ObjectAnimator.ofFloat(recyclerView, "alpha", 0f, 1f).apply {
            duration = AppConfig.ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(recyclerView, "translationY", 30f, 0f).apply {
            duration = AppConfig.ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}
