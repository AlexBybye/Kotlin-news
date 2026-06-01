package com.example.homework.data.remote.dto

data class NewsDetailDto(
    val id: String?,
    val title: String?,
    val summary: String?,
    val coverImageUrl: String?,
    val source: String?,
    val author: String?,
    val category: String?,
    val publishTime: String?,
    val content: List<String>?,
    val contentUrl: String?,
    val relatedArticles: List<NewsArticleDto>?
)
