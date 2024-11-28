package com.mushscope.view.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushscope.data.local.entity.HistoryEntity
import com.mushscope.data.source.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val historyRepository: HistoryRepository): ViewModel() {
    fun getHistory() = historyRepository.getHistory()

    fun deleteHistory(historyEntity: HistoryEntity) = viewModelScope.launch {
        historyRepository.deleteHistory(historyEntity)
    }
}