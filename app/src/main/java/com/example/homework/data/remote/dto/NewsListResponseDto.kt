package com.example.homework.data.remote.dto

data class NewsListResponseDto(
    val code: Int,
    val message: String?,
    val data: List<NewsArticleDto>?
)
