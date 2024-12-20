package com.dicoding.storyapp.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.model.StoryLocation
import kotlinx.coroutines.launch
import android.util.Log

class MapsViewModel(private val repository: MapsRepository) : ViewModel() {

    private val _stories = MutableLiveData<List<StoryLocation>>()
    val stories: LiveData<List<StoryLocation>> get() = _stories

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Fungsi untuk mengambil cerita yang memiliki lokasi
    fun fetchStoriesWithLocation(token: String) {
        viewModelScope.launch {
            try {
                val storyResponse = repository.getStoriesWithLocation(token)

                // Pastikan membaca data dari listStory, bukan stories
                val storiesWithLocation = storyResponse.listStory.mapNotNull { storyItem ->
                    val lat = storyItem.lat
                    val lon = storyItem.lon

                    if (lat != null && lon != null) {
                        StoryLocation(
                            story = storyItem,
                            lat = lat,
                            lon = lon
                        )
                    } else {
                        Log.e("MapsViewModel", "Invalid location for story: ${storyItem.name}")
                        null
                    }
                }

                // Update LiveData berdasarkan hasil filter
                if (storiesWithLocation.isNotEmpty()) {
                    _stories.postValue(storiesWithLocation)
                } else {
                    _errorMessage.postValue("No valid locations found.")
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Failed to fetch stories: ${e.message}")
                Log.e("MapsViewModel", "Error fetching stories: ${e.message}")
            }
        }
    }

}
