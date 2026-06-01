package com.example.homework.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework.data.local.entity.CachedNewsDetailEntity

@Dao
interface CachedNewsDetailDao {

    @Query("SELECT * FROM cached_news_detail WHERE newsId = :newsId LIMIT 1")
    suspend fun getById(newsId: String): CachedNewsDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CachedNewsDetailEntity)
}
