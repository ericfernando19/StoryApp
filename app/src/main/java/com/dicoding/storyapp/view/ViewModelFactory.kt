package com.dicoding.storyapp.view

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.pref.dataStore
import com.dicoding.storyapp.view.ListStory.StoryRepository
import com.dicoding.storyapp.view.ListStory.ViewModel.StoryViewModel
import com.dicoding.storyapp.view.login.LoginViewModel
import com.dicoding.storyapp.view.main.MainViewModel
import com.dicoding.storyapp.di.Injection
import com.dicoding.storyapp.view.add_story.AddStoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelFactory private constructor(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository,
    private val userPreference: UserPreference,
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(storyRepository, userRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                StoryViewModel(application, storyRepository, userRepository) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(storyRepository, userPreference) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val userPreference = UserPreference.getInstance(context.dataStore)

                val userRepository = UserRepository.getInstance(userPreference)

                var storyRepository: StoryRepository? = null
                CoroutineScope(Dispatchers.IO).launch {
                    storyRepository = Injection.provideStoryRepository(context)
                }

                while (storyRepository == null) {
                    Thread.sleep(50)
                }

                INSTANCE ?: ViewModelFactory(storyRepository!!, userRepository, userPreference, context.applicationContext as Application).also { INSTANCE = it }
            }
        }
    }
}
