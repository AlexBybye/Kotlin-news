package com.example.homework.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browse_history")
data class BrowseHistoryEntity(
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
    val lastBrowseTime: Long
)
