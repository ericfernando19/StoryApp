package com.dicoding.storyapp.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.model.StoryLocation
import kotlinx.coroutines.launch
import android.util.Log
import com.dicoding.storyapp.data.UserRepository

class MapsViewModel(
    private val repository: MapsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _stories = MutableLiveData<List<StoryLocation>>()
    val stories: LiveData<List<StoryLocation>> get() = _stories

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Fungsi untuk mengambil cerita yang memiliki lokasi
    fun fetchStoriesWithLocation() {
        viewModelScope.launch {
            try {
                // Ambil token dari UserRepository
                val token = userRepository.getUserToken()

                // Pastikan token tidak kosong atau null
                if (token.isNullOrEmpty()) {
                    _errorMessage.postValue("Token tidak tersedia, silakan login terlebih dahulu")
                } else {
                    // Log token untuk debugging
                    Log.d("MapsViewModel", "Using token: $token")

                    // Panggil API untuk mendapatkan data cerita
                    val storyResponse = repository.getStoriesWithLocation(token) // Tanpa "Bearer" jika tidak diperlukan


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
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Failed to fetch stories: ${e.message}")
                Log.e("MapsViewModel", "Error fetching stories: ${e.message}")
            }
        }
    }
}
