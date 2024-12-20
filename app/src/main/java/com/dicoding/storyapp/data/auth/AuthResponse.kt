package com.dicoding.storyapp.data.auth

import com.dicoding.storyapp.data.model.LoginResult

data class AuthResponse(
    val error: Boolean,
    val message: String,
    val loginResult: LoginResult?
)
