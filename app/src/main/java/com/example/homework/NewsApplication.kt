package com.example.homework

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.homework.data.settings.SettingsManager
import com.example.homework.work.NewsRefreshScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 应用入口，启动时根据本地设置应用深色模式。
 */
class NewsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        applyPersistedSettings()
    }

    private fun applyPersistedSettings() {
        val settingsManager = SettingsManager.getInstance(this)
        CoroutineScope(Dispatchers.Main).launch {
            val settings = settingsManager.current()
            AppCompatDelegate.setDefaultNightMode(settings.darkMode.toNightMode())
            NewsRefreshScheduler.setEnabled(this@NewsApplication, settings.autoRefresh)
        }
    }
}
