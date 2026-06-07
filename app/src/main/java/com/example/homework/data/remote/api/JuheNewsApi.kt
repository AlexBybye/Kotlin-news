package com.example.homework.data.remote.api

import com.example.homework.data.remote.dto.JuheNewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 聚合数据 · 头条新闻 Retrofit 接口。
 *
 * 文档：https://www.juhe.cn/docs/api/id/235
 */
interface JuheNewsApi {

    @GET("index")
    suspend fun getNews(
        @Query("type") type: String,
        @Query("key") key: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): JuheNewsResponseDto
}
