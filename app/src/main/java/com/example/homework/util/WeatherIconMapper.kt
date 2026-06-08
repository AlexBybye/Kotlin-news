package com.example.homework.util

/**
 * 和风天气 icon 代码 → emoji 映射。
 *
 * 用 emoji 而非图片资源，离线安全、零缺图风险。
 * icon 代码含义见 https://dev.qweather.com/docs/resource/icons/
 */
object WeatherIconMapper {

    fun toEmoji(iconCode: String): String = when (iconCode) {
        "100", "150" -> "☀️"                       // 晴
        "101", "102", "103", "153" -> "⛅"          // 多云
        "104" -> "☁️"                              // 阴
        in "300".."399" -> "🌧️"                    // 雨
        in "400".."499" -> "❄️"                    // 雪
        "500", "501", "509", "510", "514", "515" -> "🌫️" // 雾/霾
        in "502".."513" -> "🌫️"                    // 霾/沙尘
        else -> "🌡️"
    }
}
