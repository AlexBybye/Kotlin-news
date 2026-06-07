package com.example.homework.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 本地账号表。密码不明文存储，统一保存为加盐哈希值。
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val nickname: String,
    val passwordHash: String,
    val passwordSalt: String,
    val createdAt: Long
)
