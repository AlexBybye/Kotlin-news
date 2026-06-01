package com.example.homework.ui.favorite

import com.example.homework.model.NewsArticle

data class FavoriteUiState(
    val favorites: List<NewsArticle> = emptyList(),
    val histories: List<NewsArticle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
