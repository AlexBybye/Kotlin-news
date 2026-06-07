package com.example.homework.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.repository.AuthRepository
import com.example.homework.data.repository.LocalNewsRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository.createDefault(application)
    private val localNewsRepository = LocalNewsRepository.createDefault(application)

    private val _uiState = MutableLiveData(ProfileUiState())
    val uiState: LiveData<ProfileUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val user = authRepository.currentUser()
            val favoriteCount = localNewsRepository.getFavoriteCount()
            val historyCount = localNewsRepository.getBrowseHistoryCount()
            _uiState.value = ProfileUiState(
                isLoading = false,
                user = user,
                favoriteCount = favoriteCount,
                historyCount = historyCount
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value?.copy(loggedOut = true)
        }
    }
}
