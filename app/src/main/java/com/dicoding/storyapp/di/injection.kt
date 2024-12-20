package com.dicoding.storyapp.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.dicoding.storyapp.data.api.ApiConfig
import com.dicoding.storyapp.view.ListStory.StoryRepository
import com.dicoding.storyapp.data.pref.UserPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

val Context.dataStore by preferencesDataStore(name = "session")

object Injection {

    fun provideStoryRepository(context: Context): StoryRepository {
        val dataStore = context.dataStore
        val pref = UserPreference.getInstance(dataStore)

        val apiService = ApiConfig.getApiService()

        return StoryRepository.getInstance(apiService, pref)
    }
}
