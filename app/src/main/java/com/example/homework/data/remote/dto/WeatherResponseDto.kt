package com.example.homework.data.remote.dto

import com.squareup.moshi.Json

/**
 * 和风天气 实时天气接口（v7/weather/now）响应体。
 * 仅声明本项目使用到的字段。
 */
data class WeatherResponseDto(
    @Json(name = "code") val code: String?,
    @Json(name = "updateTime") val updateTime: String?,
    @Json(name = "now") val now: WeatherNowDto?
)

data class WeatherNowDto(
    @Json(name = "obsTime") val obsTime: String?,
    @Json(name = "temp") val temp: String?,
    @Json(name = "feelsLike") val feelsLike: String?,
    @Json(name = "text") val text: String?,
    @Json(name = "icon") val icon: String?,
    @Json(name = "windDir") val windDir: String?,
    @Json(name = "windScale") val windScale: String?,
    @Json(name = "humidity") val humidity: String?
)
