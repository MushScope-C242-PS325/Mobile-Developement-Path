package com.mushscope.view.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mushscope.data.remote.response.AuthResponse
import com.mushscope.data.source.UserRepository

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    fun login(email: String, password: String) = repository.login(email, password)

    fun register(name: String, email: String, password: String, photo_url: String) =
        repository.register(name, email, password, photo_url)
}