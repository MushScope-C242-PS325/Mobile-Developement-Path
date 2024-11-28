package com.mushscope.view.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mushscope.data.local.entity.HistoryEntity
import com.mushscope.databinding.HistoryItemBinding
import java.io.File

class HistoryAdapter : ListAdapter<HistoryEntity, HistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    var onDeleteClick: ((HistoryEntity) -> Unit)? = null

    inner class ViewHolder(private val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: HistoryEntity) {
            with(binding) {
                // Set the image from the file path
                val imageFile = File(history.imagePath)
                if (imageFile.exists()) {
                    imgHistory.setImageURI(Uri.fromFile(imageFile))
                }

                // Set the result text with confidence score
                val resultWithConfidence = "${history.result}\n${history.confidenceScore}"
                tvDescriptionHistory.text = resultWithConfidence

                // Set content description for accessibility
                imgHistory.contentDescription = resultWithConfidence

                // Handle delete button click
                btnDelete.setOnClickListener {
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