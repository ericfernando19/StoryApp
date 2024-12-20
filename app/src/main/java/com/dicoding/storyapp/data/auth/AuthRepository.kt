package com.dicoding.storyapp.data.auth

import android.util.Log
import com.dicoding.storyapp.data.api.ApiService
import com.dicoding.storyapp.data.model.LoginRequest
import com.dicoding.storyapp.data.model.SignupRequest
import okhttp3.ResponseBody
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    suspend fun signup(name: String, email: String, password: String): Response<AuthResponse> {
        val signupRequest = SignupRequest(name, email, password)
        return try {
            apiService.register(signupRequest)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup failed: ${e.message}")
            Response.error(500, ResponseBody.create(null, "Internal Server Error"))
        }
    }

    suspend fun login(email: String, password: String): Response<AuthResponse> {
        val loginRequest = LoginRequest(email, password)
        return try {
            apiService.login(loginRequest)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e.message}")
            Response.error(500, ResponseBody.create(null, "Internal Server Error"))
        }
    }
}
