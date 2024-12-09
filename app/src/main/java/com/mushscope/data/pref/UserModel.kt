package com.mushscope.data.pref

data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false,
    val name: String? = null,
    val photoUrl: String? = null
)