package com.example.homework.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.homework.data.local.dao.BrowseHistoryDao
import com.example.homework.data.local.dao.CachedNewsDao
import com.example.homework.data.local.dao.CachedNewsDetailDao
import com.example.homework.data.local.dao.FavoriteNewsDao
import com.example.homework.data.local.dao.SearchHistoryDao
import com.example.homework.data.local.entity.BrowseHistoryEntity
import com.example.homework.data.local.entity.CachedNewsDetailEntity
import com.example.homework.data.local.entity.CachedNewsEntity
import com.example.homework.data.local.entity.FavoriteNewsEntity
import com.example.homework.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        SearchHistoryEntity::class,
        FavoriteNewsEntity::class,
        BrowseHistoryEntity::class,
        CachedNewsEntity::class,
        CachedNewsDetailEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class HomeworkDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favoriteNewsDao(): FavoriteNewsDao
    abstract fun browseHistoryDao(): BrowseHistoryDao
    abstract fun cachedNewsDao(): CachedNewsDao
    abstract fun cachedNewsDetailDao(): CachedNewsDetailDao

    companion object {
        @Volatile
        private var INSTANCE: HomeworkDatabase? = null

        fun getInstance(context: Context): HomeworkDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HomeworkDatabase::class.java,
                    "homework.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
