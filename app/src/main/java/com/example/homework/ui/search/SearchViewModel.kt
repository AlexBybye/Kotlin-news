package com.example.homework.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.repository.SearchRepository
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val searchRepository = SearchRepository.createDefault(application)

    private val _uiState = MutableLiveData(
        SearchUiState(
            hotKeywords = DEFAULT_HOT_KEYWORDS
        )
    )
    val uiState: LiveData<SearchUiState> = _uiState

    private var lastSubmittedKeyword: String? = null

    init {
        loadHistory()
    }

    fun updateKeyword(keyword: String) {
        _uiState.value = currentState().copy(keyword = keyword, errorMessage = null)
    }

    fun submitSearch(rawKeyword: String = currentState().keyword) {
        val keyword = rawKeyword.trim()
        if (keyword.isBlank()) {
            _uiState.value = currentState().copy(keyword = keyword)
            return
        }

        lastSubmittedKeyword = keyword
        _uiState.value = currentState().copy(
            keyword = keyword,
            isLoading = true,
            errorMessage = null,
            hasSearched = true,
            results = emptyList()
        )

        viewModelScope.launch {
            when (val result = searchRepository.searchNews(keyword)) {
                is ResultWrapper.Success -> {
                    _uiState.value = currentState().copy(
                        keyword = keyword,
                        isLoading = false,
                        results = result.data,
                        errorMessage = null,
                        hasSearched = true
                    )
                    loadHistory()
                }

                is ResultWrapper.Error -> {
                    _uiState.value = currentState().copy(
                        keyword = keyword,
                        isLoading = false,
                        results = emptyList(),
                        errorMessage = result.message,
                        hasSearched = true
                    )
                }
            }
        }
    }

    fun onHistoryClicked(keyword: String) {
        updateKeyword(keyword)
        submitSearch(keyword)
    }

    fun onHotKeywordClicked(keyword: String) {
        updateKeyword(keyword)
        submitSearch(keyword)
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchRepository.clearSearchHistory()
            _uiState.value = currentState().copy(recentHistory = emptyList())
        }
    }

    fun retry() {
        lastSubmittedKeyword?.let(::submitSearch)
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = searchRepository.getRecentSearchHistory()
            _uiState.value = currentState().copy(recentHistory = history)
        }
    }

    private fun currentState(): SearchUiState {
        return _uiState.value ?: SearchUiState(hotKeywords = DEFAULT_HOT_KEYWORDS)
    }

    companion object {
        private val DEFAULT_HOT_KEYWORDS = listOf("科技", "校园", "体育", "国际", "志愿服务")
    }
}
