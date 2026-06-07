package com.example.homework.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

/**
 * 应用设置项持久化（DataStore）。
 *
 * 覆盖深色模式、正文字号、仅 Wi-Fi 加载大图、定时刷新等设置。
 */
class SettingsManager(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            darkMode = DarkMode.fromStorageValue(preferences[KEY_DARK_MODE]),
            fontScale = FontScale.fromStorageValue(preferences[KEY_FONT_SCALE]),
            wifiOnlyImages = preferences[KEY_WIFI_ONLY_IMAGES] ?: false,
            autoRefresh = preferences[KEY_AUTO_REFRESH] ?: false,
            useBackend = preferences[KEY_USE_BACKEND] ?: false
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setDarkMode(mode: DarkMode) {
        context.settingsDataStore.edit { it[KEY_DARK_MODE] = mode.storageValue }
    }

    suspend fun setFontScale(scale: FontScale) {
        context.settingsDataStore.edit { it[KEY_FONT_SCALE] = scale.storageValue }
        // 同步镜像写入，供 attachBaseContext 阶段同步读取字号。
        syncPrefs().edit().putInt(KEY_FONT_SCALE_SYNC, scale.storageValue).apply()
    }

    suspend fun setWifiOnlyImages(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_WIFI_ONLY_IMAGES] = enabled }
        syncPrefs().edit().putBoolean(KEY_WIFI_ONLY_SYNC, enabled).apply()
    }

    suspend fun setAutoRefresh(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_AUTO_REFRESH] = enabled }
    }

    suspend fun setUseBackend(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_USE_BACKEND] = enabled }
    }

    /** 同步读取字号，仅供 Activity.attachBaseContext 等无法挂起的场景使用。 */
    fun fontScaleSync(): FontScale {
        val value = syncPrefs().getInt(KEY_FONT_SCALE_SYNC, FontScale.STANDARD.storageValue)
        return FontScale.fromStorageValue(value)
    }

    /** 同步读取「仅 Wi-Fi 加载大图」开关，供图片加载时快速判断。 */
    fun wifiOnlyImagesSync(): Boolean {
        return syncPrefs().getBoolean(KEY_WIFI_ONLY_SYNC, false)
    }

    private fun syncPrefs() =
        context.getSharedPreferences("app_settings_sync", Context.MODE_PRIVATE)

    companion object {
        private val KEY_DARK_MODE = intPreferencesKey("dark_mode")
        private val KEY_FONT_SCALE = intPreferencesKey("font_scale")
        private val KEY_WIFI_ONLY_IMAGES = booleanPreferencesKey("wifi_only_images")
        private val KEY_AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        private val KEY_USE_BACKEND = booleanPreferencesKey("use_backend")
        private const val KEY_FONT_SCALE_SYNC = "font_scale_sync"
        private const val KEY_WIFI_ONLY_SYNC = "wifi_only_sync"

        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
