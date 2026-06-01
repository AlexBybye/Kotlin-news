package com.example.homework.data.remote.api

import com.example.homework.data.remote.dto.NewsListResponseDto

interface NewsApiService {
    suspend fun getNews(
        category: String,
        page: Int = 1,
        pageSize: Int = 10
    ): NewsListResponseDto
}
