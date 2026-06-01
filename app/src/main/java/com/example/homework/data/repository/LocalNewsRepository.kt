package com.example.homework.data.repository

import android.content.Context
import com.example.homework.data.local.HomeworkDatabase
import com.example.homework.data.local.dao.BrowseHistoryDao
import com.example.homework.data.local.dao.FavoriteNewsDao
import com.example.homework.data.mapper.LocalNewsMapper
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsDetail

class LocalNewsRepository(
    private val favoriteNewsDao: FavoriteNewsDao,
    private val browseHistoryDao: BrowseHistoryDao
) {

    suspend fun toggleFavorite(detail: NewsDetail): Boolean {
        val isFavorite = favoriteNewsDao.isFavorite(detail.id)
        return if (isFavorite) {
            favoriteNewsDao.deleteById(detail.id)
            false
        } else {
            favoriteNewsDao.insert(LocalNewsMapper.toFavoriteEntity(detail))
            true
        }
    }

    suspend fun isFavorite(newsId: String): Boolean {
        return favoriteNewsDao.isFavorite(newsId)
    }

    suspend fun saveBrowseHistory(detail: NewsDetail) {
        browseHistoryDao.insert(LocalNewsMapper.toBrowseHistoryEntity(detail))
        browseHistoryDao.trimToLatest20()
    }

    suspend fun getFavoriteNewsList(): List<NewsArticle> {
        return favoriteNewsDao.getAllFavorites().map(LocalNewsMapper::toNewsArticle)
    }

    suspend fun getBrowseHistoryList(): List<NewsArticle> {
        return browseHistoryDao.getRecentHistory().map(LocalNewsMapper::toNewsArticle)
    }

    companion object {
        fun createDefault(context: Context): LocalNewsRepository {
            val database = HomeworkDatabase.getInstance(context)
            return LocalNewsRepository(
                favoriteNewsDao = database.favoriteNewsDao(),
                browseHistoryDao = database.browseHistoryDao()
            )
        }
    }
}
