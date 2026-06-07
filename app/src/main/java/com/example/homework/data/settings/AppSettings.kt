package com.example.homework.data.settings

import androidx.appcompat.app.AppCompatDelegate

data class AppSettings(
    val darkMode: DarkMode = DarkMode.FOLLOW_SYSTEM,
    val fontScale: FontScale = FontScale.STANDARD,
    val wifiOnlyImages: Boolean = false,
    val autoRefresh: Boolean = false,
    val useBackend: Boolean = false
)

enum class DarkMode(val storageValue: Int, val displayName: String) {
    FOLLOW_SYSTEM(0, "跟随系统"),
    LIGHT(1, "浅色"),
    DARK(2, "深色");

    fun toNightMode(): Int = when (this) {
        FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }

    companion object {
        fun fromStorageValue(value: Int?): DarkMode =
            entries.firstOrNull { it.storageValue == value } ?: FOLLOW_SYSTEM
    }
}

enum class FontScale(val storageValue: Int, val displayName: String, val scale: Float) {
    SMALL(0, "小", 0.9f),
    STANDARD(1, "标准", 1.0f),
    LARGE(2, "大", 1.15f),
    EXTRA_LARGE(3, "特大", 1.3f);

    companion object {
        fun fromStorageValue(value: Int?): FontScale =
            entries.firstOrNull { it.storageValue == value } ?: STANDARD
    }
}
