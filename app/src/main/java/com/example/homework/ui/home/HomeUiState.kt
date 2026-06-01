package com.example.homework.ui.home

import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsCategory

data class HomeUiState(
    val selectedCategory: NewsCategory = NewsCategory.RECOMMEND,
    val categoryList: List<NewsCategory> = NewsCategory.entries,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val articles: List<NewsArticle> = emptyList(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val isFromCache: Boolean = false
)
