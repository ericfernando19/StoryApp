package com.dicoding.storyapp.view.ListStory.ViewModel

import ListStoryItem
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.view.ListStory.StoryRepository
import com.dicoding.storyapp.data.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class StoryViewModel(
    application: Application,
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> get() = _stories

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Menambahkan Flow untuk data Paging
    fun getPagedStories(): Flow<PagingData<ListStoryItem>> {
        return flow {
            try {
                val token = userRepository.getUserToken() // Panggilan suspend function
                if (token.isNullOrEmpty()) {
                    _errorMessage.postValue("Token tidak tersedia, silakan login terlebih dahulu")
                    emit(PagingData.empty())
                } else {
                    emitAll(storyRepository.getStoriesPaged(token).cachedIn(viewModelScope))
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Terjadi kesalahan")
                emit(PagingData.empty())
            }
        }
    }

    // Fungsi untuk mengambil cerita dengan lokasi
    fun fetchStoriesWithLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val token = userRepository.getUserToken()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token tidak tersedia, silakan login."
                    return@launch
                }
                val formattedToken = "Bearer $token"
                val response = storyRepository.getStoriesWithLocation(formattedToken)
                response.listStory?.let {
                    _stories.value = it
                    Log.d("StoryViewModel", "Fetched ${it.size} stories with location.")
                } ?: run {
                    _errorMessage.value = "Tidak ada data lokasi yang ditemukan."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Terjadi kesalahan"
                Log.e("StoryViewModel", "Error fetching stories: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi untuk mengambil cerita berdasarkan userId
    fun fetchStories(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val token = userRepository.getUserToken()

                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token tidak tersedia, silakan login terlebih dahulu"
                    return@launch
                }

                val formattedToken = "Bearer $token"
                val response = storyRepository.getStories(formattedToken, userId)

                if (response.listStory.isNullOrEmpty()) {
                    _errorMessage.value = "Tidak ada cerita ditemukan"
                } else {
                    _stories.value = response.listStory
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
