package com.example.homework.data.repository

import android.content.Context
import com.example.homework.BuildConfig
import com.example.homework.data.remote.api.QWeatherApi
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.remote.network.WeatherRetrofitClient
import com.example.homework.data.settings.DEFAULT_WEATHER_CITY_NAME
import com.example.homework.data.settings.DEFAULT_WEATHER_LOCATION_ID
import com.example.homework.data.settings.SettingsManager
import com.example.homework.model.WeatherCity
import com.example.homework.model.WeatherNow
import java.util.Locale

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
    private val settingsManager = SettingsManager.getInstance(context)

    /**
     * 获取实时天气。
     * 默认读取 DataStore 中保存的天气城市；也可显式传入城市 ID/经纬度与展示城市名。
     */
    suspend fun getCurrentWeather(
        location: String? = null,
        cityName: String? = null
    ): ResultWrapper<WeatherNow> {
        val settings = if (location == null || cityName == null) settingsManager.current() else null
        val targetLocation = location ?: settings?.weatherLocationId ?: DEFAULT_LOCATION
        val targetCityName = cityName ?: settings?.weatherCityName ?: DEFAULT_CITY_NAME

        if (apiKey.isBlank()) {
            return cachedOrError("尚未配置天气 API Key。", targetCityName)
        }

        return runCatching {
            val response = api.getCurrentWeather(location = targetLocation, apiKey = apiKey)
            val now = response.now
            if (response.code != "200" || now == null) {
                cachedOrError("天气接口返回异常（code=${response.code}）。", targetCityName)
            } else {
                val weather = WeatherNow(
                    cityName = targetCityName,
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
            cachedOrError(it.message ?: "获取天气失败，请稍后重试。", targetCityName)
        }
    }

    suspend fun resolveCityByCoordinates(latitude: Double, longitude: Double): ResultWrapper<WeatherCity> {
        if (apiKey.isBlank()) {
            return ResultWrapper.Error("尚未配置天气 API Key，无法根据定位解析城市。")
        }

        return runCatching {
            // 和风天气 GeoAPI 经纬度顺序为 longitude,latitude。
            val coordinate = String.format(Locale.US, "%.2f,%.2f", longitude, latitude)
            val response = api.lookupCity(location = coordinate, apiKey = apiKey)
            val cityDto = response.locations?.firstOrNull()
            if (response.code != "200" || cityDto?.id.isNullOrBlank()) {
                ResultWrapper.Error("定位城市解析失败（code=${response.code}）。")
            } else {
                ResultWrapper.Success(
                    WeatherCity(
                        id = cityDto.id.orEmpty(),
                        name = cityDto.name.orEmpty().ifBlank { "当前位置" },
                        adm1 = cityDto.adm1.orEmpty(),
                        adm2 = cityDto.adm2.orEmpty()
                    )
                )
            }
        }.getOrElse {
            ResultWrapper.Error(it.message ?: "定位城市解析失败，请稍后重试。")
        }
    }

    /** 仅读取本地缓存的天气（供 UI 首屏快速展示）。 */
    fun getCachedWeather(): WeatherNow? {
        val targetCityName = settingsManager.weatherCityNameSync()
        return WeatherCache.read(context)?.takeIf { it.cityName == targetCityName }
    }

    private fun cachedOrError(message: String, cityName: String): ResultWrapper<WeatherNow> {
        val cached = WeatherCache.read(context)
        return if (cached != null && cached.cityName == cityName) {
            ResultWrapper.Success(cached)
        } else {
            ResultWrapper.Error(message)
        }
    }

    companion object {
        // 广州（华南理工大学所在地）。可按需替换为其它城市 ID。
        const val DEFAULT_LOCATION = DEFAULT_WEATHER_LOCATION_ID
        const val DEFAULT_CITY_NAME = DEFAULT_WEATHER_CITY_NAME

        fun createDefault(context: Context): WeatherRepository {
            return WeatherRepository(context.applicationContext)
        }
    }
}
