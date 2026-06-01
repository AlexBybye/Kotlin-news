package com.example.homework.ui.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.repository.LocalNewsRepository
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val localNewsRepository = LocalNewsRepository.createDefault(application)

    private val _uiState = MutableLiveData(FavoriteUiState(isLoading = true))
    val uiState: LiveData<FavoriteUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = currentState().copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                FavoriteUiState(
                    favorites = localNewsRepository.getFavoriteNewsList(),
                    histories = localNewsRepository.getBrowseHistoryList(),
                    isLoading = false,
                    errorMessage = null
                )
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure { throwable ->
                _uiState.value = FavoriteUiState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "当前无法读取收藏和浏览历史，请稍后重试。"
                )
            }
        }
    }

    private fun currentState(): FavoriteUiState {
        return _uiState.value ?: FavoriteUiState()
    }
}
