package com.dicoding.storyapp.data.model

import ListStoryItem
import com.google.gson.annotations.SerializedName

data class StoryResponse(
    @field:SerializedName("listStory")
    val listStory: List<ListStoryItem> = emptyList(),

    // 'stories' berisi daftar StoryLocation yang mencakup cerita dan koordinat lokasi
    @field:SerializedName("stories")
    val stories: List<StoryLocation> = emptyList()
)
