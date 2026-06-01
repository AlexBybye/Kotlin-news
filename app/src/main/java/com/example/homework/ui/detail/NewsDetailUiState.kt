package com.example.homework.ui.detail

import com.example.homework.model.NewsDetail

data class NewsDetailUiState(
    val isLoading: Boolean = false,
    val detail: NewsDetail? = null,
    val errorMessage: String? = null,
    val isFromCache: Boolean = false
)
