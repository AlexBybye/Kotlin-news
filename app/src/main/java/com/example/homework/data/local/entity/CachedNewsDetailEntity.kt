package com.example.homework.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_news_detail")
data class CachedNewsDetailEntity(
    @PrimaryKey
    val newsId: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val author: String?,
    val source: String,
    val category: String,
    val publishTime: String,
    val contentUrl: String?,
    val contentJson: String,
    val relatedArticlesJson: String,
    val cachedAt: Long
)
