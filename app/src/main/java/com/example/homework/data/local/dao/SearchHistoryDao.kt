package com.example.homework.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework.data.local.entity.SearchHistoryEntity

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY lastSearchTime DESC LIMIT 10")
    suspend fun getRecentHistory(): List<SearchHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query(
        """
        DELETE FROM search_history
        WHERE keyword NOT IN (
            SELECT keyword FROM search_history
            ORDER BY lastSearchTime DESC
            LIMIT 10
        )
        """
    )
    suspend fun trimToLatest10()
}
