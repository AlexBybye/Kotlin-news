package com.example.homework.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.repository.NewsRepository
import com.example.homework.model.NewsCategory
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val newsRepository: NewsRepository = NewsRepository.createDefault(application)

    private val _uiState = MutableLiveData(HomeUiState(isLoading = true))
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        loadNews()
    }

    fun loadNews(
        category: NewsCategory = currentState().selectedCategory,
        forceRefresh: Boolean = false
    ) {
        val previousState = currentState()
        val sameCategory = previousState.selectedCategory == category
        val preservedArticles = if (sameCategory) previousState.articles else emptyList()

        _uiState.value = previousState.copy(
            selectedCategory = category,
            isLoading = !forceRefresh && preservedArticles.isEmpty(),
            isRefreshing = forceRefresh,
            errorMessage = null,
            isEmpty = false,
            articles = preservedArticles,
            isFromCache = if (preservedArticles.isNotEmpty()) previousState.isFromCache else false
        )

        viewModelScope.launch {
            when (val result = newsRepository.getNews(category)) {
                is ResultWrapper.Success -> {
                    val articles = result.data.value
                    _uiState.value = currentState().copy(
                        selectedCategory = category,
                        isLoading = false,
                        isRefreshing = false,
                        articles = articles,
                        errorMessage = null,
                        isEmpty = articles.isEmpty(),
                        isFromCache = result.data.isFromCache
                    )
                }

                is ResultWrapper.Error -> {
                    _uiState.value = currentState().copy(
                        selectedCategory = category,
                        isLoading = false,
                        isRefreshing = false,
                        articles = preservedArticles,
                        errorMessage = result.message,
                        isEmpty = false,
                        isFromCache = if (preservedArticles.isNotEmpty()) previousState.isFromCache else false
                    )
                }
            }
        }
    }

    fun onCategorySelected(category: NewsCategory) {
        val state = currentState()
        if (state.selectedCategory == category && state.articles.isNotEmpty()) {
            return
        }
        loadNews(category = category)
    }

    fun refresh() {
        loadNews(category = currentState().selectedCategory, forceRefresh = true)
    }

    fun retry() {
        loadNews(category = currentState().selectedCategory)
    }

    private fun currentState(): HomeUiState {
        return _uiState.value ?: HomeUiState()
    }
}
