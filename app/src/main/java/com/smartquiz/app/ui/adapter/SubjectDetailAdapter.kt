package com.smartquiz.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartquiz.app.databinding.ItemSubjectDetailBinding
import com.smartquiz.app.ui.viewmodel.SubjectInfo

class SubjectDetailAdapter(
    private val onItemClick: (SubjectInfo) -> Unit
) : ListAdapter<SubjectInfo, SubjectDetailAdapter.SubjectDetailViewHolder>(SubjectDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectDetailViewHolder {
        val binding = ItemSubjectDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubjectDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubjectDetailViewHolder(
        private val binding: ItemSubjectDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(subjectInfo: SubjectInfo) {
            binding.apply {
                textSubjectName.text = subjectInfo.name
                textQuestionCount.text = "${subjectInfo.questionCount}"
                
                if (subjectInfo.averageScore != null) {
                    textAverageScore.text = "${subjectInfo.averageScore.toInt()}%"
                } else {
                    textAverageScore.text = "Chưa có"
                }
                
                textQuizCount.text = "${subjectInfo.quizCount}"
            }
        }
    }

    private class SubjectDetailDiffCallback : DiffUtil.ItemCallback<SubjectInfo>() {
        override fun areItemsTheSame(oldItem: SubjectInfo, newItem: SubjectInfo): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: SubjectInfo, newItem: SubjectInfo): Boolean {
            return oldItem == newItem
        }
    }
}
