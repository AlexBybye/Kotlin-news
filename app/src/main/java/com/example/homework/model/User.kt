package com.example.homework.model

/**
 * 登录态用户信息（不含密码）。
 */
data class User(
    val username: String,
    val nickname: String,
    val createdAt: Long
)
