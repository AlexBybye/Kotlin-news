package com.example.homework.model

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val author: String?,
    val source: String,
    val category: NewsCategory,
    val publishTime: String,
    val contentUrl: String?,
    val isTop: Boolean = false
)
