package com.dicoding.storyapp.data.pref

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun getUserToken() {
        viewModelScope.launch {
            try {
                val token = userRepository.getUserToken()
                Log.d("UserViewModel", "Fetched token: $token")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching token: ${e.message}")
            }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            try {
                userRepository.saveSession(user)
                Log.d("UserViewModel", "Session saved: ${user.email}")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error saving session: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout()
                Log.d("UserViewModel", "User logged out")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error logging out: ${e.message}")
            }
        }
    }
}
