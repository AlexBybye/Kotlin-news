package com.example.homework.ui.auth

import com.example.homework.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val authenticatedUser: User? = null,
    val errorMessage: String? = null
)
