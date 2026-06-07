package com.example.homework.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.repository.AuthRepository
import com.example.homework.model.User
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository.createDefault(application)

    private val _uiState = MutableLiveData(AuthUiState())
    val uiState: LiveData<AuthUiState> = _uiState

    fun login(username: String, password: String) {
        if (_uiState.value?.isLoading == true) return
        _uiState.value = AuthUiState(isLoading = true)

        viewModelScope.launch {
            when (val result = authRepository.login(username, password)) {
                is ResultWrapper.Success -> emitSuccess(result.data)
                is ResultWrapper.Error -> emitError(result.message)
            }
        }
    }

    fun register(
        username: String,
        nickname: String,
        password: String,
        confirmPassword: String
    ) {
        if (_uiState.value?.isLoading == true) return
        _uiState.value = AuthUiState(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.register(username, nickname, password, confirmPassword)
            when (result) {
                is ResultWrapper.Success -> emitSuccess(result.data)
                is ResultWrapper.Error -> emitError(result.message)
            }
        }
    }

    fun onErrorConsumed() {
        _uiState.value = _uiState.value?.copy(errorMessage = null)
    }

    fun onNavigationConsumed() {
        _uiState.value = _uiState.value?.copy(authenticatedUser = null)
    }

    private fun emitSuccess(user: User) {
        _uiState.value = AuthUiState(isLoading = false, authenticatedUser = user)
    }

    private fun emitError(message: String) {
        _uiState.value = AuthUiState(isLoading = false, errorMessage = message)
    }
}
