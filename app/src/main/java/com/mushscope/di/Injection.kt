package com.mushscope.di

import android.content.Context
import com.mushscope.data.local.room.HistoryDatabase
import com.mushscope.data.source.HistoryRepository

object Injection {
    fun provideRepository(context : Context) : HistoryRepository {
        val db = HistoryDatabase.getInstance(context)
        val dao = db.HistoryDao()
        return HistoryRepository.getInstance(dao)
    }
}

