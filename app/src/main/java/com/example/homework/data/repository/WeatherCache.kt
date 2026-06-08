package com.example.homework.data.repository

import android.content.Context
import com.example.homework.model.WeatherNow
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * 天气本地缓存。使用 SharedPreferences + Moshi 序列化，
 * 供后台 Worker 写入、UI 首屏与离线场景读取。
 */
internal object WeatherCache {

    private const val PREFS_NAME = "weather_cache"
    private const val KEY_WEATHER_JSON = "weather_json"

    private val adapter by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(WeatherNow::class.java)
    }

    fun save(context: Context, weather: WeatherNow) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WEATHER_JSON, adapter.toJson(weather))
            .apply()
    }

    fun read(context: Context): WeatherNow? {
        val json = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WEATHER_JSON, null) ?: return null
        return runCatching { adapter.fromJson(json) }.getOrNull()
    }
}
