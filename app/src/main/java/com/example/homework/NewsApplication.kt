package com.example.homework

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.homework.data.config.AppConfig
import com.example.homework.data.settings.SettingsManager
import com.example.homework.work.NewsRefreshScheduler
import com.example.homework.work.WeatherSyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 应用入口，启动时根据本地设置应用深色模式、定时刷新与数据来源开关，并注册天气后台同步。
 */
class NewsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        applyPersistedSettings()
        WeatherSyncScheduler.schedule(this)
    }

    private fun applyPersistedSettings() {
        val settingsManager = SettingsManager.getInstance(this)
        CoroutineScope(Dispatchers.Main).launch {
            val settings = settingsManager.current()
            AppCompatDelegate.setDefaultNightMode(settings.darkMode.toNightMode())
            AppConfig.useBackend = settings.useBackend
            NewsRefreshScheduler.setEnabled(this@NewsApplication, settings.autoRefresh)
        }
    }
}
