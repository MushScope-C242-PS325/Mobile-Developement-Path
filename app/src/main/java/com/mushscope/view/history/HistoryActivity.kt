package com.mushscope.view.history

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mushscope.R
import com.mushscope.data.local.entity.HistoryEntity
import com.mushscope.databinding.ActivityHistoryBinding
import com.mushscope.utils.ViewModelFactory
import com.mushscope.view.adapter.HistoryAdapter
import java.io.File

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter().apply {
            onDeleteClick = { history ->
                showDeleteConfirmationDialog(history)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        observeHistoryData()
    }


    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeHistoryData() {
        viewModel.getHistory().observe(this) { historyList ->
            if (historyList.isNotEmpty()) {
                historyAdapter.submitList(historyList)
                showEmptyState(false)
            } else {
                showEmptyState(true)
            }
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.apply {
                recyclerView.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                recyclerView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmationDialog(history: HistoryEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(getString(R.string.delete_confirmation_message))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteHistory(history)
            }
            .show()
    }

    private fun deleteHistory(history: HistoryEntity) {
        viewModel.deleteHistory(history)
        // Delete the image file
        val imageFile = File(history.imagePath)
        if (imageFile.exists()) {
            imageFile.delete()
        }
        showSnackbar(getString(R.string.history_deleted))
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }
}
