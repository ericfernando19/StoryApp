package com.dicoding.storyapp.data.model

import ListStoryItem

data class StoryLocation(
    val story: ListStoryItem,
    val lat: Double?,
    val lon: Double?
)