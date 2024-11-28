package com.mushscope.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mushscope.data.pref.SettingPreference
import com.mushscope.data.source.HistoryRepository
import com.mushscope.di.Injection
import com.mushscope.view.history.HistoryViewModel
import com.mushscope.view.main.MainViewModel
import com.mushscope.view.profile.ProfileViewModel
import android.content.Context
import com.mushscope.data.pref.dataStore
import com.mushscope.view.result.ResultViewModel

class ViewModelFactory(
    private val pref: SettingPreference,
    private val historyRepository: HistoryRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(pref) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(historyRepository) as T
            }
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                ResultViewModel(historyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            val dataStore = context.dataStore // Mengambil DataStore langsung dari ekstensi
            return instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    SettingPreference.getInstance(dataStore),
                    Injection.provideRepository(context)
                ).also { instance = it }
            }
        }
    }
}
