package com.example.homework.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework.data.local.entity.LikedNewsEntity

@Dao
interface LikedNewsDao {

    @Query("SELECT EXISTS(SELECT 1 FROM liked_news WHERE newsId = :newsId)")
    suspend fun isLiked(newsId: String): Boolean

    @Query("SELECT COUNT(*) FROM liked_news")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: LikedNewsEntity)

    @Query("DELETE FROM liked_news WHERE newsId = :newsId")
    suspend fun deleteById(newsId: String)
}
