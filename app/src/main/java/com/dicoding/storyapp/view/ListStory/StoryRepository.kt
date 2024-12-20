package com.dicoding.storyapp.view.ListStory

import ListStoryItem
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.storyapp.data.api.ApiService
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.model.StoryLocation
import com.dicoding.storyapp.data.model.StoryResponse
import com.dicoding.storyapp.data.paging.StoryPagingSource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class StoryRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    // Method untuk mendapatkan daftar cerita berdasarkan token dan userId
    suspend fun getStories(token: String, userId: String): StoryResponse {
        return apiService.getStories(token, userId)
    }

    // Mendapatkan cerita dengan lokasi
    suspend fun getStoriesWithLocation(token: String): StoryResponse {
        val response = apiService.getStoriesWithLocation(token = token, location = 1)
        response.listStory?.forEach { story ->
            Log.d("StoryRepository", "Story Name: ${story.name}, Lat: ${story.lat}, Lon: ${story.lon}")
        }
        return response
    }


    // Menambahkan Paging untuk mendapatkan data cerita
    fun getStoriesPaged(token: String): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }


    // Mengunggah cerita baru dengan gambar
    suspend fun uploadStory(
        token: String,
        description: RequestBody,
        file: MultipartBody.Part
    ): Response<Unit> {
        val formattedToken = "Bearer $token"
        return apiService.uploadStory(formattedToken, description, file)
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        // Singleton pattern untuk mendapatkan instance StoryRepository
        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference).also { instance = it }
            }
    }
}
