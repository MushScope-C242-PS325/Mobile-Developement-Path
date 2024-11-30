package com.mushscope.data.remote.retrofit

import com.mushscope.data.remote.request.LoginRequest
import com.mushscope.data.remote.request.RegisterRequest
import com.mushscope.data.remote.response.AuthResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("register")
    suspend fun register(
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("password") password: String,
        @Part photo: MultipartBody.Part
    ): AuthResponse

    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): AuthResponse
}