package com.mushscope.data.source

import androidx.lifecycle.LiveData
import com.mushscope.data.local.entity.HistoryEntity
import com.mushscope.data.local.room.HistoryDao
import java.io.File

class HistoryRepository(
    private val historyDao: HistoryDao
) {
    fun getHistory() : LiveData<List<HistoryEntity>> {
        return historyDao.getHistory()
    }

    suspend fun insertHistory(historyEntity: HistoryEntity) {
        historyDao.insertHistory(historyEntity)
    }

    suspend fun deleteHistory(historyEntity: HistoryEntity) {
        historyDao.deleteHistory(historyEntity)

        val fileData = File(historyEntity.imagePath)
        if (fileData.exists()) fileData.delete()
    }

    companion object {
        @Volatile
        private var instance: HistoryRepository? = null
        fun getInstance(
            historyDao: HistoryDao
        ): HistoryRepository =
            instance?: synchronized(this) {
                instance?: HistoryRepository(
                    historyDao
                )
            }.also { instance = it }
    }
}