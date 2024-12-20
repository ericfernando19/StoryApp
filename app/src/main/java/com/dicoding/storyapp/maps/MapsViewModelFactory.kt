package com.dicoding.storyapp.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.view.ListStory.StoryRepository

class MapsViewModelFactory(private val mapsRepository: MapsRepository) : ViewModelProvider.Factory {

    // Menyediakan ViewModel yang dibutuhkan
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(mapsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
