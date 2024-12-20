package com.dicoding.storyapp.data.api

import com.dicoding.storyapp.data.auth.AuthResponse
import com.dicoding.storyapp.data.model.LoginRequest
import com.dicoding.storyapp.data.model.SignupRequest
import com.dicoding.storyapp.data.model.StoryLocation
import com.dicoding.storyapp.data.model.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("register")
    suspend fun register(@Body signupRequest: SignupRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): StoryResponse

    @Multipart
    @POST("stories")
    suspend fun uploadStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("location") location: Int = 1
    ): StoryResponse



    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): StoryResponse


}
