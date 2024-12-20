package com.dicoding.storyapp.utils

import android.content.Context
import android.content.SharedPreferences

class TokenPreference(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TokenPreferences", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("token").apply()
    }
}
