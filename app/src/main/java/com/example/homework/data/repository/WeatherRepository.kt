package com.example.homework.data.repository

import android.content.Context
import com.example.homework.BuildConfig
import com.example.homework.data.remote.api.QWeatherApi
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.remote.network.WeatherRetrofitClient
import com.example.homework.model.WeatherNow

/**
 * 天气数据仓库。
 *
 * 调用和风天气接口获取实时天气，成功后写入本地缓存（SharedPreferences）；
 * 请求失败时回退到最近一次缓存，保证弱网/无网时仍能展示天气。
 * 与项目其它 Repository 一致，统一返回 [ResultWrapper]。
 */
class WeatherRepository(
    private val context: Context,
    private val api: QWeatherApi = WeatherRetrofitClient.api,
    private val apiKey: String = BuildConfig.QWEATHER_API_KEY
) {

    /**
     * 获取实时天气。
     * @param location 和风天气城市 ID 或经纬度，默认广州（101280101）。
     * @param cityName 展示用城市名（接口 now 不含城市名，由调用方提供）。
     */
    suspend fun getCurrentWeather(
        location: String = DEFAULT_LOCATION,
        cityName: String = DEFAULT_CITY_NAME
    ): ResultWrapper<WeatherNow> {
        if (apiKey.isBlank()) {
            return cachedOrError("尚未配置天气 API Key。")
        }

        return runCatching {
            val response = api.getCurrentWeather(location = location, apiKey = apiKey)
            val now = response.now
            if (response.code != "200" || now == null) {
                cachedOrError("天气接口返回异常（code=${response.code}）。")
            } else {
                val weather = WeatherNow(
                    cityName = cityName,
                    temperature = now.temp ?: "--",
                    feelsLike = now.feelsLike ?: "--",
                    text = now.text ?: "--",
                    iconCode = now.icon ?: "",
                    windDir = now.windDir ?: "",
                    windScale = now.windScale ?: "",
                    humidity = now.humidity ?: "",
                    updateTime = response.updateTime ?: ""
                )
                WeatherCache.save(context, weather)
                ResultWrapper.Success(weather)
            }
        }.getOrElse {
            cachedOrError(it.message ?: "获取天气失败，请稍后重试。")
        }
    }

    /** 仅读取本地缓存的天气（供 UI 首屏快速展示）。 */
    fun getCachedWeather(): WeatherNow? = WeatherCache.read(context)

    private fun cachedOrError(message: String): ResultWrapper<WeatherNow> {
        val cached = WeatherCache.read(context)
        return if (cached != null) ResultWrapper.Success(cached) else ResultWrapper.Error(message)
    }

    companion object {
        // 广州（华南理工大学所在地）。可按需替换为其它城市 ID。
        const val DEFAULT_LOCATION = "101280101"
        const val DEFAULT_CITY_NAME = "广州"

        fun createDefault(context: Context): WeatherRepository {
            return WeatherRepository(context.applicationContext)
        }
    }
}
