package com.example.homework.ui.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.config.AppConfig
import com.example.homework.data.repository.LocalCacheRepository
import com.example.homework.data.settings.AppSettings
import com.example.homework.data.settings.DarkMode
import com.example.homework.data.settings.FontScale
import com.example.homework.data.settings.SettingsManager
import com.example.homework.work.NewsRefreshScheduler
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager.getInstance(application)
    private val localCacheRepository = LocalCacheRepository.createDefault(application)

    private val _settings = MutableLiveData(AppSettings())
    val settings: LiveData<AppSettings> = _settings

    private val _cacheCount = MutableLiveData(0)
    val cacheCount: LiveData<Int> = _cacheCount

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    init {
        viewModelScope.launch {
            _settings.value = settingsManager.current()
            refreshCacheCount()
        }
    }

    fun onDarkModeSelected(mode: DarkMode) {
        viewModelScope.launch {
            settingsManager.setDarkMode(mode)
            _settings.value = settingsManager.current()
            AppCompatDelegate.setDefaultNightMode(mode.toNightMode())
        }
    }

    fun onFontScaleSelected(scale: FontScale) {
        viewModelScope.launch {
            settingsManager.setFontScale(scale)
            _settings.value = settingsManager.current()
            _message.value = "字号已更新，重新打开页面后生效。"
        }
    }

    fun onWifiOnlyImagesChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setWifiOnlyImages(enabled)
            _settings.value = settingsManager.current()
        }
    }

    fun onAutoRefreshChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setAutoRefresh(enabled)
            _settings.value = settingsManager.current()
            NewsRefreshScheduler.setEnabled(getApplication(), enabled)
        }
    }

    fun onUseBackendChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setUseBackend(enabled)
            AppConfig.useBackend = enabled
            _settings.value = settingsManager.current()
            _message.value = if (enabled) {
                "已切换为后端模式，请重新登录以连接后端账号。"
            } else {
                "已切换为本地模式。"
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            localCacheRepository.clearCache()
            refreshCacheCount()
            _message.value = "缓存已清除。"
        }
    }

    fun onMessageConsumed() {
        _message.value = null
    }

    private suspend fun refreshCacheCount() {
        _cacheCount.value = localCacheRepository.getCachedItemCount()
    }
}
