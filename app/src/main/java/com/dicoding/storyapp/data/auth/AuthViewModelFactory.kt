package com.dicoding.storyapp.ui.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.auth.AuthRepository
import com.dicoding.storyapp.data.pref.UserPreference

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val userPreference = UserPreference.getInstance(dataStore)
            return AuthViewModel(authRepository, userPreference) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
