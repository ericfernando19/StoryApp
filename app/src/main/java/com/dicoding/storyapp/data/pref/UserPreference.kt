package com.dicoding.storyapp.data.pref

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    private val EMAIL_KEY = stringPreferencesKey("email")
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private const val TAG = "UserPreference"

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreference(dataStore).also { INSTANCE = it }
            }
        }
    }

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = user.email
            preferences[TOKEN_KEY] = user.token
            preferences[USER_ID_KEY] = user.userId
        }
        Log.d(TAG, "Session saved: email=${user.email}, token=${user.token}, userId=${user.userId}")
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data
            .map { preferences ->
                val email = preferences[EMAIL_KEY] ?: ""
                val token = preferences[TOKEN_KEY] ?: ""
                val userId = preferences[USER_ID_KEY] ?: ""
                Log.d(TAG, "Fetched session: email=$email, token=$token, userId=$userId")
                UserModel(email = email, token = token, userId = userId)
            }
    }

    suspend fun getUserToken(): String {
        val token = dataStore.data
            .map { preferences -> preferences[TOKEN_KEY] ?: "" }
            .first()
        Log.d(TAG, "Fetched token: $token")
        return token
    }

    suspend fun getUserId(): String? {
        val userId = dataStore.data
            .map { preferences -> preferences[USER_ID_KEY] ?: "" }
            .first()
        Log.d(TAG, "Fetched userId: $userId")
        return userId
    }

    suspend fun clearSession() {
        try {
            // Menghapus seluruh data dari DataStore
            dataStore.edit { preferences ->
                preferences.clear() // Clear all stored preferences
            }
            Log.d(TAG, "Session cleared from DataStore.")

            // Verifikasi penghapusan sesi
            val session = getSession().first()  // Mengambil sesi pengguna setelah clear
            if (session.token.isEmpty() && session.userId.isEmpty()) {
                Log.d(TAG, "Session successfully cleared.")
            } else {
                Log.d(TAG, "Session still exists after clearing. Token: ${session.token}, UserId: ${session.userId}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error clearing session: ${e.message}")
        }
    }

    suspend fun logout() {
        clearSession() // Pastikan sesi dihapus sebelum logout
        Log.d(TAG, "User logged out.")

        // Jika perlu, tambahkan logika untuk mengarahkan pengguna ke halaman login atau halaman lainnya
    }
}
