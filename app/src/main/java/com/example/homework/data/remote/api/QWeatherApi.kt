package com.example.homework.data.remote.api

import com.example.homework.data.remote.dto.WeatherResponseDto
import com.example.homework.data.remote.dto.WeatherCityLookupResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 和风天气 RESTful 接口。
 *
 * 文档：https://dev.qweather.com/docs/api/weather/weather-now/
 */
interface QWeatherApi {

    @GET("v7/weather/now")
    suspend fun getCurrentWeather(
        @Query("location") location: String,
        @Query("key") apiKey: String,
        @Query("lang") lang: String = "zh"
    ): WeatherResponseDto

    @GET("geo/v2/city/lookup")
    suspend fun lookupCity(
        @Query("location") location: String,
        @Query("key") apiKey: String,
        @Query("lang") lang: String = "zh",
        @Query("number") number: Int = 1
    ): WeatherCityLookupResponseDto
}
