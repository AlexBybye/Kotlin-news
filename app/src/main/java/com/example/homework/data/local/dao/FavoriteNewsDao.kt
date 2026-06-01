package com.example.homework.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework.data.local.entity.FavoriteNewsEntity

@Dao
interface FavoriteNewsDao {

    @Query("SELECT * FROM favorite_news ORDER BY favoritedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteNewsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteNewsEntity)

    @Query("DELETE FROM favorite_news WHERE newsId = :newsId")
    suspend fun deleteById(newsId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_news WHERE newsId = :newsId)")
    suspend fun isFavorite(newsId: String): Boolean
}
