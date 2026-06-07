package com.example.homework.model

data class NewsDetail(
    val id: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val source: String,
    val author: String?,
    val category: NewsCategory,
    val publishTime: String,
    val content: List<String>,
    val contentUrl: String?,
    val isCollected: Boolean = false,
    val isLiked: Boolean = false,
    val relatedArticles: List<NewsArticle> = emptyList()
)
