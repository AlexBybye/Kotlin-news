package com.example.newsbackend.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * NewsAPI（https://newsapi.org）top-headlines / everything 接口响应结构。
 * 仅声明本项目使用到的字段。
 */
@Serializable
data class NewsApiResponse(
    val status: String,
    val totalResults: Int = 0,
    val articles: List<NewsApiArticle> = emptyList(),
    val code: String? = null,
    val message: String? = null
)

@Serializable
data class NewsApiArticle(
    val source: NewsApiSource? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null
)

@Serializable
data class NewsApiSource(
    val id: String? = null,
    val name: String? = null
)
