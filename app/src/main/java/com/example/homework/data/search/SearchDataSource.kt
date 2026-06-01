package com.example.homework.data.search

import com.example.homework.data.remote.dto.NewsArticleDto
import com.example.homework.data.remote.network.ResultWrapper

interface SearchDataSource {
    suspend fun searchNews(keyword: String): ResultWrapper<List<NewsArticleDto>>
}
