package com.example.homework.ui.discover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.repository.NewsRepository
import kotlinx.coroutines.launch

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val newsRepository = NewsRepository.createDefault(application)

    private val _uiState = MutableLiveData(DiscoverUiState(isLoading = true))
    val uiState: LiveData<DiscoverUiState> = _uiState

    init {
        load()
    }

    fun load() {
        _uiState.value = DiscoverUiState(isLoading = true, hotKeywords = HOT_KEYWORDS)
        viewModelScope.launch {
            when (val result = newsRepository.getTrendingNews()) {
                is ResultWrapper.Success -> {
                    _uiState.value = DiscoverUiState(
                        isLoading = false,
                        hotKeywords = HOT_KEYWORDS,
                        trendingArticles = result.data,
                        errorMessage = null
                    )
                }

                is ResultWrapper.Error -> {
                    _uiState.value = DiscoverUiState(
                        isLoading = false,
                        hotKeywords = HOT_KEYWORDS,
                        trendingArticles = emptyList(),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    companion object {
        private val HOT_KEYWORDS = listOf(
            "校园科技节", "大模型", "校运会", "志愿服务", "国际交流", "城市更新", "毕业季"
        )
    }
}
