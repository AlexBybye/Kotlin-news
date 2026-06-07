package com.example.homework.ui.discover

import com.example.homework.model.NewsArticle

data class DiscoverUiState(
    val isLoading: Boolean = true,
    val hotKeywords: List<String> = emptyList(),
    val trendingArticles: List<NewsArticle> = emptyList(),
    val errorMessage: String? = null
)
