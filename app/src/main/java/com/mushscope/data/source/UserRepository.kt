package com.mushscope.data.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import retrofit2.HttpException
import com.google.gson.Gson
import com.mushscope.data.pref.UserModel
import com.mushscope.data.pref.UserPreference
import com.mushscope.data.remote.request.LoginRequest
import com.mushscope.data.remote.request.RegisterRequest
import com.mushscope.data.remote.response.AuthResponse
import com.mushscope.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    fun register(name: String, email: String, password: String, photoPath: String): LiveData<Result<AuthResponse>> = liveData {
        emit(Result.Loading)
        try {
            // Prepare photo as MultipartBody.Part
            val photoPart = File(photoPath).let { file ->
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("photo", file.name, requestFile)
            }

            // Call API
            val response = apiService.register(name, email, password, photoPart)

            if (response.error==false) {
                response.loginResult?.let {
                    saveSession(
                        UserModel(
                            email = email,
                            token = it.token ?: "",
                            isLogin = true
                        )
                    )
                }
                emit(Result.Success(response))
            } else {
                emit(Result.Error(response.message ?: "Registration failed"))
            }
        } catch (e: HttpException) {
            emit(Result.Error(handleHttpException(e)))
        } catch (e: Exception) {
            emit(Result.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }


    fun login(email: String, password: String): LiveData<Result<AuthResponse>> = liveData {
        emit(Result.Loading)
        try {
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)

            if (response.error == false) {
                val loginResult = response.loginResult
                loginResult?.let {
                    // Save session
                    val user = UserModel(
                        email = email,
                        token = it.token ?: "",
                        isLogin = true
                    )
                    saveSession(user)
                }
                emit(Result.Success(response))
            } else {
                emit(Result.Error(response.message ?: "Login failed"))
            }
        } catch (e: HttpException) {
            Log.e("UserRepository", "HTTP Exception: ${e.message}")
            val errorResponse = handleHttpException(e)
            emit(Result.Error(errorResponse))
        } catch (e: Exception) {
            Log.e("UserRepository", "General Exception: ${e.message}")
            emit(Result.Error("Error: ${e.localizedMessage}"))
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    private fun handleHttpException(e: HttpException): String {
        val errorResponse = e.response()?.errorBody()?.string()
        val gson = Gson()
        return try {
            gson.fromJson(errorResponse, AuthResponse::class.java)?.message ?: "Unknown error"
        } catch (exception: Exception) {
            "Error parsing HTTP exception response"
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
    }
}