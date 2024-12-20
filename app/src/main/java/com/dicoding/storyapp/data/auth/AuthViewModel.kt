package com.dicoding.storyapp.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.auth.AuthRepository
import com.dicoding.storyapp.data.auth.AuthResponse
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _loginResponse = MutableLiveData<AuthResponse>()
    val loginResponse: LiveData<AuthResponse> get() = _loginResponse

    private val _signupResponse = MutableLiveData<AuthResponse>()
    val signupResponse: LiveData<AuthResponse> get() = _signupResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Sending login request with email: $email and password: $password")
                val response: Response<AuthResponse> = authRepository.login(email, password)

                Log.d("AuthViewModel", "Full API Response: ${response.body()}")

                if (response.isSuccessful) {
                    val authResponse = response.body() ?: AuthResponse(
                        error = true,
                        message = "Login failed",
                        loginResult = null
                    )
                    Log.d("AuthViewModel", "Login Response: ${authResponse.message}, Token: ${authResponse.loginResult?.token}")

                    val token = authResponse.loginResult?.token
                    val userId = authResponse.loginResult?.userId ?: ""
                    if (!token.isNullOrEmpty()) {
                        _loginResponse.postValue(authResponse)
                        saveSession(email, token,userId)
                    } else {
                        Log.e("AuthViewModel", "Token is null or empty")
                        _loginResponse.postValue(
                            AuthResponse(
                                error = true,
                                message = "Login berhasil, namun token tidak ditemukan",
                                loginResult = null
                            )
                        )
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("AuthViewModel", "Login Error Code: ${response.code()}, Message: $errorMessage")
                    _loginResponse.postValue(
                        AuthResponse(
                            error = true,
                            message = errorMessage,
                            loginResult = null
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login Error: ", e)
                _loginResponse.postValue(
                    AuthResponse(
                        error = true,
                        message = "Terjadi kesalahan tak terduga",
                        loginResult = null
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveSession(email: String, token: String,userid: String) {
        viewModelScope.launch {
            try {
                val user = UserModel(email = email, token = token, userId = userid)
                userPreference.saveSession(user)
                Log.d("AuthViewModel", "Session saved: email=$email, token=$token")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to save session: ", e)
            }
        }
    }

    fun signup(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Sending signup request with name: $name, email: $email")
                val response: Response<AuthResponse> = authRepository.signup(name, email, password)

                if (response.isSuccessful) {
                    val authResponse = response.body() ?: AuthResponse(
                        error = true,
                        message = "Signup failed",
                        loginResult = null
                    )
                    Log.d("AuthViewModel", "Signup Response: ${authResponse.message}")

                    _signupResponse.postValue(authResponse)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("AuthViewModel", "Signup Error Code: ${response.code()}, Message: $errorMessage")
                    _signupResponse.postValue(
                        AuthResponse(
                            error = true,
                            message = errorMessage,
                            loginResult = null
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Signup Error: ", e)
                _signupResponse.postValue(
                    AuthResponse(
                        error = true,
                        message = "An unexpected error occurred",
                        loginResult = null
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

}
