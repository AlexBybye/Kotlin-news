package com.example.homework.data.remote.api

import com.example.homework.data.remote.dto.AuthDataDto
import com.example.homework.data.remote.dto.BackendResponseDto
import com.example.homework.data.remote.dto.LoginRequestDto
import com.example.homework.data.remote.dto.NewsArticleDto
import com.example.homework.data.remote.dto.NewsDetailDto
import com.example.homework.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 自建后端接口（Ktor）。新闻由后端代理 NewsAPI，账号由后端管理。
 */
interface BackendApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): BackendResponseDto<AuthDataDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): BackendResponseDto<AuthDataDto>

    @GET("news")
    suspend fun getNews(@Query("category") category: String): BackendResponseDto<List<NewsArticleDto>>

    @GET("news/detail/{id}")
    suspend fun getNewsDetail(@Path("id") id: String): BackendResponseDto<NewsDetailDto>
}
