package com.example.homework.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework.data.local.entity.BrowseHistoryEntity

@Dao
interface BrowseHistoryDao {

    @Query("SELECT * FROM browse_history ORDER BY lastBrowseTime DESC LIMIT 20")
    suspend fun getRecentHistory(): List<BrowseHistoryEntity>

    @Query("SELECT COUNT(*) FROM browse_history")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BrowseHistoryEntity)

    @Query(
        """
        DELETE FROM browse_history
        WHERE newsId NOT IN (
            SELECT newsId FROM browse_history
            ORDER BY lastBrowseTime DESC
            LIMIT 20
        )
        """
    )
    suspend fun trimToLatest20()
}
