package com.example.homework.data.remote.dto

data class NewsArticleDto(
    val id: String?,
    val title: String?,
    val summary: String?,
    val coverImageUrl: String?,
    val author: String?,
    val source: String?,
    val category: String?,
    val publishTime: String?,
    val contentUrl: String?,
    val isTop: Boolean? = false
)
