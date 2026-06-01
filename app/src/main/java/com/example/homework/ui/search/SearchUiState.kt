package com.example.homework.ui.search

import com.example.homework.model.NewsArticle
import com.example.homework.model.SearchHistory

data class SearchUiState(
    val keyword: String = "",
    val isLoading: Boolean = false,
    val results: List<NewsArticle> = emptyList(),
    val recentHistory: List<SearchHistory> = emptyList(),
    val hotKeywords: List<String> = emptyList(),
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)
