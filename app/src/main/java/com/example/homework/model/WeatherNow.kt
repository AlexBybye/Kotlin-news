package com.example.homework.model

/**
 * 当前天气（领域模型，UI 直接消费）。
 */
data class WeatherNow(
    val cityName: String,
    val temperature: String,
    val feelsLike: String,
    val text: String,
    val iconCode: String,
    val windDir: String,
    val windScale: String,
    val humidity: String,
    val updateTime: String
)
