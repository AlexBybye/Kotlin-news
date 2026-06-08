package com.example.homework.data.remote.dto

import com.squareup.moshi.Json

/**
 * 和风天气城市搜索/经纬度反查接口（geo/v2/city/lookup）响应体。
 */
data class WeatherCityLookupResponseDto(
    @Json(name = "code") val code: String?,
    @Json(name = "location") val locations: List<WeatherCityDto>?
)

data class WeatherCityDto(
    @Json(name = "name") val name: String?,
    @Json(name = "id") val id: String?,
    @Json(name = "adm1") val adm1: String?,
    @Json(name = "adm2") val adm2: String?
)
