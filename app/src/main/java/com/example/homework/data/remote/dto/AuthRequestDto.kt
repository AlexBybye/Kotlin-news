package com.example.homework.data.remote.dto

/** 登录请求体。 */
data class LoginRequestDto(
    val username: String,
    val password: String
)

/** 注册请求体。 */
data class RegisterRequestDto(
    val username: String,
    val nickname: String?,
    val password: String
)
