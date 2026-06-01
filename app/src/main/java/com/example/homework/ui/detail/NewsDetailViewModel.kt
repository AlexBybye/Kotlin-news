package com.example.homework.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.repository.LocalNewsRepository
import com.example.homework.data.repository.NewsRepository
import kotlinx.coroutines.launch

class NewsDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val newsRepository: NewsRepository = NewsRepository.createDefault(application)
    private val localNewsRepository: LocalNewsRepository =
        LocalNewsRepository.createDefault(application)

    private val _uiState = MutableLiveData(NewsDetailUiState(isLoading = true))
    val uiState: LiveData<NewsDetailUiState> = _uiState

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private var currentNewsId: String? = null

    fun loadDetail(newsId: String) {
        currentNewsId = newsId
        _uiState.value = NewsDetailUiState(isLoading = true)

        viewModelScope.launch {
            when (val result = newsRepository.getNewsDetail(newsId)) {
                is ResultWrapper.Success -> {
                    val isCollected = localNewsRepository.isFavorite(result.data.value.id)
                    val detail = result.data.value.copy(isCollected = isCollected)
                    localNewsRepository.saveBrowseHistory(detail)
                    _uiState.value = NewsDetailUiState(
                        isLoading = false,
                        detail = detail,
                        errorMessage = null,
                        isFromCache = result.data.isFromCache
                    )
                }

                is ResultWrapper.Error -> {
                    _uiState.value = NewsDetailUiState(
                        isLoading = false,
                        detail = null,
                        errorMessage = result.message,
                        isFromCache = false
                    )
                }
            }
        }
    }

    fun retry() {
        currentNewsId?.let(::loadDetail)
    }

    fun toggleCollect() {
        val currentState = _uiState.value ?: return
        val detail = currentState.detail ?: return

        viewModelScope.launch {
            val isCollected = localNewsRepository.toggleFavorite(detail)
            _uiState.value = currentState.copy(
                detail = detail.copy(isCollected = isCollected)
            )
            _message.value = if (isCollected) {
                getApplication<Application>().getString(com.example.homework.R.string.detail_collect_added)
            } else {
                getApplication<Application>().getString(com.example.homework.R.string.detail_collect_removed)
            }
        }
    }

    fun onMessageConsumed() {
        _message.value = null
    }
}
