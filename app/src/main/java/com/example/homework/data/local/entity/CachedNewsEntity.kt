package com.example.homework.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_news",
    indices = [
        Index(value = ["category"]),
        Index(value = ["newsId", "category"], unique = true)
    ]
)
data class CachedNewsEntity(
    @PrimaryKey(autoGenerate = true)
    val cacheId: Long = 0,
    val newsId: String,
    val category: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val author: String?,
    val source: String,
    val publishTime: String,
    val contentUrl: String?,
    val isTop: Boolean,
    val displayOrder: Int,
    val cachedAt: Long
)
