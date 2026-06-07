package com.example.homework.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 点赞记录。记录用户点过赞的新闻，用于点赞状态持久化。
 */
@Entity(tableName = "liked_news")
data class LikedNewsEntity(
    @PrimaryKey
    val newsId: String,
    val likedAt: Long
)
