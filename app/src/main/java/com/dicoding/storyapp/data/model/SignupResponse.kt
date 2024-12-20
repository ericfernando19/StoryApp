package com.dicoding.storyapp.data.model

data class SignupResponse(
    val error: Boolean,
    val message: String,
    val loginResult: LoginResult?
)
