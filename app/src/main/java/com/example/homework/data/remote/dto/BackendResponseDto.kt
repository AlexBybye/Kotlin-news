package com.example.homework.data.remote.dto

/**
 * 后端统一响应包装：code=0 表示成功。
 */
data class BackendResponseDto<T>(
    val code: Int,
    val message: String?,
    val data: T?
)

/** 登录 / 注册返回的数据体。 */
data class AuthDataDto(
    val token: String,
    val user: BackendUserDto
)

data class BackendUserDto(
    val username: String,
    val nickname: String,
    val createdAt: Long
)
