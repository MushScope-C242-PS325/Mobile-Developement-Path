package com.mushscope.data.remote.response

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("loginResult")
    val loginResult: LoginResult? = null
)

data class LoginResult(
    @field:SerializedName("userId")
    val userId: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("token")
    val token: String? = null,

    @field:SerializedName("photo_url")
    val photoUrl: String? = null
)