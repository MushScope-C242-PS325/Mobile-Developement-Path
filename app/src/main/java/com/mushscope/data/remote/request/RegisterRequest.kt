package com.mushscope.data.remote.request

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("password")
    val password: String,

    @field:SerializedName("photo_url")
    val photoUrl: String
)