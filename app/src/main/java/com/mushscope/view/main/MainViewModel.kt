package com.mushscope.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mushscope.data.pref.ThemePreference
import com.mushscope.data.pref.UserModel
import com.mushscope.data.source.UserRepository

class MainViewModel(
    private val pref: ThemePreference,
    private val userRepository: UserRepository
) : ViewModel() {
    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }
    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }
}