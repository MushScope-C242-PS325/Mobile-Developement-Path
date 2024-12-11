package com.mushscope.view.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mushscope.data.pref.ThemePreference
import com.mushscope.data.pref.UserModel
import com.mushscope.data.source.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val pref: ThemePreference,
    private val userRepository: UserRepository
) : ViewModel() {
    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            pref.saveThemeSetting(isDarkModeActive)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun getUserSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }
}