package com.mushscope.di

import android.content.Context
import com.mushscope.data.local.room.HistoryDatabase
import com.mushscope.data.pref.UserPreference
import com.mushscope.data.pref.themeDataStore
import com.mushscope.data.remote.retrofit.ApiConfig
import com.mushscope.data.source.HistoryRepository
import com.mushscope.data.source.UserRepository

object Injection {
    fun provideHistoryRepository(context : Context) : HistoryRepository {
        val db = HistoryDatabase.getInstance(context)
        val dao = db.HistoryDao()
        return HistoryRepository.getInstance(dao)
    }
    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.themeDataStore)
        val apiService = ApiConfig.getApiService(pref)
        return UserRepository.getInstance(pref, apiService)
    }
}

