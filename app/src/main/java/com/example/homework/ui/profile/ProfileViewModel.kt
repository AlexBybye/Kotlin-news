package com.example.homework.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homework.ui.model.PagePlaceholderUiState

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableLiveData(
        PagePlaceholderUiState(
            pageTitle = "我的",
            pageSubtitle = "设置与个人中心",
            tips = "下一步将接入主题切换、字号设置和缓存管理。"
        )
    )
    val uiState: LiveData<PagePlaceholderUiState> = _uiState
}
