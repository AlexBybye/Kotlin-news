package com.example.homework.data.remote.dto

import com.squareup.moshi.Json

/**
 * 聚合数据头条新闻接口响应体。
 */
data class JuheNewsResponseDto(
    @Json(name = "reason") val reason: String?,
    @Json(name = "result") val result: JuheNewsResultDto?,
    @Json(name = "error_code") val errorCode: Int?
)

data class JuheNewsResultDto(
    @Json(name = "stat") val stat: String?,
    @Json(name = "data") val data: List<JuheNewsItemDto>?
)

data class JuheNewsItemDto(
    @Json(name = "uniquekey") val uniqueKey: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "date") val date: String?,
    @Json(name = "category") val category: String?,
    @Json(name = "author_name") val authorName: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "thumbnail_pic_s") val thumbnailPic: String?
)
