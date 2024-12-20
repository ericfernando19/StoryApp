package com.dicoding.storyapp.view.add_story

import ListStoryItem
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.view.ListStory.StoryActivity
import com.dicoding.storyapp.view.ListStory.StoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryViewModel(
    private val storyRepository: StoryRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> = _uploadStatus

    private val _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> = _stories

    fun uploadStory(description: String, file: MultipartBody.Part, context: Context) {
        // Validasi deskripsi kosong
        if (description.isBlank()) {
            Log.e("AddStoryViewModel", "Description is empty.")
            _uploadStatus.postValue(false)
            return
        }

        viewModelScope.launch {
            try {
                val token = userPreference.getUserToken()

                if (token.isEmpty()) {
                    Log.e("AddStoryViewModel", "Token is empty, cannot upload story.")
                    _uploadStatus.postValue(false)
                    return@launch
                }

                val descriptionRequestBody = withContext(Dispatchers.IO) {
                    description.toRequestBody("text/plain".toMediaTypeOrNull())
                }

                val response = storyRepository.uploadStory(token, descriptionRequestBody, file)

                if (response.isSuccessful) {
                    Log.d("AddStoryViewModel", "Story uploaded successfully.")
                    _uploadStatus.postValue(true)

                    val userId = userPreference.getUserId()
                    if (!userId.isNullOrEmpty()) {
                        val intent = Intent(context, StoryActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        context.startActivity(intent)
                    } else {
                        Log.e("AddStoryViewModel", "User ID is null or empty.")
                    }

                    fetchStories(token)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("AddStoryViewModel", "Failed to upload story: $errorMessage")
                    _uploadStatus.postValue(false)
                }
            } catch (e: CancellationException) {
                Log.e("AddStoryViewModel", "Job was cancelled: ${e.message}")
                _uploadStatus.postValue(false)
            } catch (e: Exception) {
                Log.e("AddStoryViewModel", "Error during story upload: ${e.message}")
                _uploadStatus.postValue(false)
            }
        }
    }

    fun fetchStories(token: String) {
        viewModelScope.launch {
            try {
                // Pastikan userId ada
                val userId = userPreference.getUserId()

                if (userId.isNullOrEmpty()) {
                    Log.e("AddStoryViewModel", "User ID is missing or empty, cannot fetch stories.")
                    _stories.postValue(emptyList())
                    return@launch
                }


                val response = storyRepository.getStories(token, userId)

                if (response.listStory.isNotEmpty()) {
                    Log.d("AddStoryViewModel", "Fetched stories successfully.")
                    _stories.postValue(response.listStory)
                } else {
                    Log.e("AddStoryViewModel", "No stories available or failed to fetch stories.")
                    _stories.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e("AddStoryViewModel", "Error fetching stories: ${e.message}")
                _stories.postValue(emptyList())
            }
        }
    }
}
