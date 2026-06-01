package com.example.homework.ui.discover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homework.ui.model.PagePlaceholderUiState

class DiscoverViewModel : ViewModel() {

    private val _uiState = MutableLiveData(
        PagePlaceholderUiState(
            pageTitle = "发现",
            pageSubtitle = "热点专题与精选内容",
            tips = "下一步将接入热搜词、专题聚合和热点内容。"
        )
    )
    val uiState: LiveData<PagePlaceholderUiState> = _uiState
}
