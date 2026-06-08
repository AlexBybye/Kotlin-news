package com.example.homework.model

/**
 * 和风天气城市位置（用于城市选择与定位解析）。
 */
data class WeatherCity(
    val id: String,
    val name: String,
    val adm1: String = "",
    val adm2: String = ""
) {
    val displayName: String
        get() = when {
            adm2.isNotBlank() && adm2 != name -> "$adm2·$name"
            adm1.isNotBlank() && adm1 != name -> "$adm1·$name"
            else -> name
        }
}
