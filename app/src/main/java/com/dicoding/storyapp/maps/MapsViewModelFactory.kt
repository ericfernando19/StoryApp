package com.dicoding.storyapp.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.UserRepository

class MapsViewModelFactory(private val mapsRepository: MapsRepository, private val userRepository: UserRepository) : ViewModelProvider.Factory {

    // Menyediakan ViewModel yang dibutuhkan
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(mapsRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}