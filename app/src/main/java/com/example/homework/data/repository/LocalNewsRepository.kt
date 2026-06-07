package com.example.homework.data.repository

import android.content.Context
import com.example.homework.data.local.HomeworkDatabase
import com.example.homework.data.local.dao.BrowseHistoryDao
import com.example.homework.data.local.dao.FavoriteNewsDao
import com.example.homework.data.local.dao.LikedNewsDao
import com.example.homework.data.local.entity.LikedNewsEntity
import com.example.homework.data.mapper.LocalNewsMapper
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsDetail

class LocalNewsRepository(
    private val favoriteNewsDao: FavoriteNewsDao,
    private val browseHistoryDao: BrowseHistoryDao,
    private val likedNewsDao: LikedNewsDao
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

    /** 切换点赞状态，返回切换后是否为已点赞。 */
    suspend fun toggleLike(newsId: String): Boolean {
        val isLiked = likedNewsDao.isLiked(newsId)
        return if (isLiked) {
            likedNewsDao.deleteById(newsId)
            false
        } else {
            likedNewsDao.insert(
                LikedNewsEntity(newsId = newsId, likedAt = System.currentTimeMillis())
            )
            true
        }
    }

    suspend fun isLiked(newsId: String): Boolean {
        return likedNewsDao.isLiked(newsId)
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

    suspend fun getFavoriteCount(): Int = favoriteNewsDao.count()

    suspend fun getBrowseHistoryCount(): Int = browseHistoryDao.count()

    companion object {
        fun createDefault(context: Context): LocalNewsRepository {
            val database = HomeworkDatabase.getInstance(context)
            return LocalNewsRepository(
                favoriteNewsDao = database.favoriteNewsDao(),
                browseHistoryDao = database.browseHistoryDao(),
                likedNewsDao = database.likedNewsDao()
            )
        }
    }
}
