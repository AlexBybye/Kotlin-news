package com.example.newsbackend.model

import kotlinx.serialization.Serializable

// ---------- 通用响应封装 ----------

/**
 * 统一响应格式：code=0 表示成功，非 0 表示业务失败。
 * 与 Android 端 NewsListResponseDto 的约定保持一致。
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String? = null,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "success") =
            ApiResponse(code = 0, message = message, data = data)

        fun <T> error(message: String, code: Int = 1) =
            ApiResponse<T>(code = code, message = message, data = null)
    }
}

// ---------- 认证 ----------

@Serializable
data class RegisterRequest(
    val username: String,
    val nickname: String? = null,
    val password: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthData(
    val token: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val username: String,
    val nickname: String,
    val createdAt: Long
)

// ---------- 新闻 ----------

@Serializable
data class NewsArticleDto(
    val id: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String? = null,
    val author: String? = null,
    val source: String,
    val category: String,
    val publishTime: String,
    val contentUrl: String? = null,
    val isTop: Boolean = false
)

@Serializable
data class NewsDetailDto(
    val id: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String? = null,
    val source: String,
    val author: String? = null,
    val category: String,
    val publishTime: String,
    val content: List<String>,
    val contentUrl: String? = null,
    val relatedArticles: List<NewsArticleDto> = emptyList()
)
