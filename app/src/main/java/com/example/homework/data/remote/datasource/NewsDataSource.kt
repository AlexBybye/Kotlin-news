package com.example.homework.data.remote.datasource

import com.example.homework.data.remote.dto.NewsDetailDto
import com.example.homework.data.remote.dto.NewsListResponseDto
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.model.NewsCategory

interface NewsDataSource {
    suspend fun getNews(category: NewsCategory): ResultWrapper<NewsListResponseDto>
    suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetailDto>
}
