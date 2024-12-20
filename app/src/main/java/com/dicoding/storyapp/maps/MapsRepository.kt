package com.dicoding.storyapp.maps

import android.util.Log
import com.dicoding.storyapp.data.api.ApiService
import com.dicoding.storyapp.data.model.StoryLocation
import com.dicoding.storyapp.data.model.StoryResponse

class MapsRepository(
    private val apiService: ApiService
) {

    // Fungsi untuk mengambil cerita dengan lokasi
    suspend fun getStoriesWithLocation(token: String): StoryResponse {
        val response = apiService.getStories("Bearer $token")
        Log.d("MapsRepository", "API Response: $response")
        return response
    }

}
