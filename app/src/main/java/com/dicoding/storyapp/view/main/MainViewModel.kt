package com.dicoding.storyapp.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.view.ListStory.StoryRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Mengambil sesi pengguna
    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun getUserToken() {
        viewModelScope.launch {
            val token = userRepository.getUserToken()
            Log.d("MainViewModel", "Token: $token")
        }
    }
}
