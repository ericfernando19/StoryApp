package com.dicoding.storyapp.data.api

import android.content.SharedPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "https://story-api.dicoding.dev/v1/"

    private var sharedPreferences: SharedPreferences? = null

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = sharedPreferences?.getString("auth_token", null)
            val newRequest = chain.request().newBuilder()
                .apply {
                    token?.let { addHeader("Authorization", "Bearer $it") }
                }
                .build()
            chain.proceed(newRequest)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun initializeSharedPreferences(sharedPreferences: SharedPreferences) {
        this.sharedPreferences = sharedPreferences
    }
}

