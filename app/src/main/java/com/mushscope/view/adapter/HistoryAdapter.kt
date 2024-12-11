package com.mushscope.view.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mushscope.R
import com.mushscope.data.local.entity.HistoryEntity
import com.mushscope.databinding.HistoryItemBinding
import com.mushscope.view.animation.animateButton
import java.io.File
import androidx.core.content.ContextCompat

class HistoryAdapter : ListAdapter<HistoryEntity, HistoryAdapter.ViewHolder>(DIFF_CALLBACK) {
    var onDeleteClick: ((HistoryEntity) -> Unit)? = null

    inner class ViewHolder(private val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: HistoryEntity) {
            with(binding) {
                val imageFile = File(history.imagePath)
                if (imageFile.exists()) {
                    imgHistory.setImageURI(Uri.fromFile(imageFile))
                }

                val resultWithConfidence = "${history.result}\n${history.confidenceScore}"
                resultText.text = resultWithConfidence

                when (history.result.replace("Result: ", "").trim().lowercase()) {
                    "poisonous" -> {
                        binding.imgResultIcon.setImageResource(R.drawable.ic_warning)
                        binding.mcResultHistory.setCardBackgroundColor(
                            ContextCompat.getColor(binding.root.context, R.color.red_primary)
                        )
                        binding.resultText.setTextColor(
                            ContextCompat.getColor(binding.root.context, android.R.color.white)
                        )
                    }
                    else -> {
                        binding.imgResultIcon.setImageResource(R.drawable.ic_save)
                        binding.mcResultHistory.setCardBackgroundColor(
                            ContextCompat.getColor(binding.root.context, R.color.green_primary)
                        )
                        binding.resultText.setTextColor(
                            ContextCompat.getColor(binding.root.context, android.R.color.white)
                        )
                    }
                }

                btnDelete.setOnClickListener {
                    animateButton(binding.btnDelete)
                    onDeleteClick?.invoke(history)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = getItem(position)
        holder.bind(history)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoryEntity>() {
            override fun areItemsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}