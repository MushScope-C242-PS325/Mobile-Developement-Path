package com.mushscope.view.result
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushscope.data.local.entity.HistoryEntity
import com.mushscope.data.source.HistoryRepository
import kotlinx.coroutines.launch

class ResultViewModel(private val historyRepository: HistoryRepository): ViewModel() {
    fun insertHistory(historyEntity: HistoryEntity) = viewModelScope.launch {
        historyRepository.insertHistory(historyEntity)
    }
}