package com.smartquiz.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartquiz.app.data.entities.Quiz
import com.smartquiz.app.databinding.ItemRecentQuizBinding
import java.text.SimpleDateFormat
import java.util.*

class RecentQuizAdapter(
    private val onItemClick: (Quiz) -> Unit
) : ListAdapter<Quiz, RecentQuizAdapter.QuizViewHolder>(QuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemRecentQuizBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuizViewHolder(
        private val binding: ItemRecentQuizBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(quiz: Quiz) {
            binding.apply {
                textSubject.text = quiz.subject
                textScore.text = "${quiz.score.toInt()}%"
                textDifficulty.text = getDifficultyText(quiz.difficulty)
                textDate.text = formatDate(quiz.completedAt)
            }
        }

        private fun getDifficultyText(difficulty: String): String {
            return when (difficulty) {
                "easy" -> "Dễ"
                "medium" -> "Trung bình"
                "hard" -> "Khó"
                else -> difficulty
            }
        }

        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 24 * 60 * 60 * 1000 -> "Hôm nay"
                diff < 2 * 24 * 60 * 60 * 1000 -> "Hôm qua"
                else -> {
                    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    private class QuizDiffCallback : DiffUtil.ItemCallback<Quiz>() {
        override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem == newItem
        }
    }
}
