package com.example.homework.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework.data.local.entity.CachedNewsEntity

@Dao
interface CachedNewsDao {

    @Query("SELECT * FROM cached_news WHERE category = :category ORDER BY displayOrder ASC")
    suspend fun getByCategory(category: String): List<CachedNewsEntity>

    @Query("DELETE FROM cached_news WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedNewsEntity>)
}
