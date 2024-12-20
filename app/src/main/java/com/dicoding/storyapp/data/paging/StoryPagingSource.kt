package com.dicoding.storyapp.data.paging

import ListStoryItem
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.storyapp.data.api.ApiService

class StoryPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, ListStoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: 1
            val response = apiService.getStories(
                token = "Bearer $token",
                page = position,
                size = params.loadSize
            )

            // Mengecek apakah response.listStory kosong
            val stories = response.listStory ?: emptyList()

            // Jika jumlah item yang diterima lebih sedikit dari size, tidak ada halaman berikutnya
            val nextKey = if (stories.size < params.loadSize) null else position + 1
            val prevKey = if (position == 1) null else position - 1

            LoadResult.Page(
                data = stories,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}

