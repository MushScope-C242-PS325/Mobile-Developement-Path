package com.mushscope.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mushscope.data.pref.ThemePreference
import com.mushscope.data.source.HistoryRepository
import com.mushscope.di.Injection
import com.mushscope.view.history.HistoryViewModel
import com.mushscope.view.main.MainViewModel
import com.mushscope.view.profile.ProfileViewModel
import android.content.Context
import com.mushscope.data.pref.themeDataStore
import com.mushscope.data.source.UserRepository
import com.mushscope.view.auth.AuthViewModel
import com.mushscope.view.result.ResultViewModel

class ViewModelFactory(
    private val pref: ThemePreference,
    private val historyRepository: HistoryRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(pref, userRepository) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref, userRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(historyRepository) as T
            }
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                ResultViewModel(historyRepository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            val themeDataStore = context.themeDataStore
            return instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    ThemePreference.getInstance(themeDataStore),
                    Injection.provideHistoryRepository(context),
                    Injection.provideUserRepository(context)
                ).also { instance = it }
            }
        }
    }
}
