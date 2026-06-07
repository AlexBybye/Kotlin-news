package com.example.homework.ui.profile

import com.example.homework.model.User

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val favoriteCount: Int = 0,
    val historyCount: Int = 0,
    val loggedOut: Boolean = false
)
