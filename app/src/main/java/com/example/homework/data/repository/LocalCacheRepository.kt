package com.example.homework.data.repository

import android.content.Context
import com.example.homework.data.local.HomeworkDatabase
import com.example.homework.data.local.dao.CachedNewsDao
import com.example.homework.data.local.dao.CachedNewsDetailDao
import com.example.homework.data.mapper.CacheNewsMapper
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsCategory
import com.example.homework.model.NewsDetail

class LocalCacheRepository(
    private val cachedNewsDao: CachedNewsDao,
    private val cachedNewsDetailDao: CachedNewsDetailDao
) {

    suspend fun saveCategoryNews(category: NewsCategory, articles: List<NewsArticle>) {
        val cachedAt = System.currentTimeMillis()
        cachedNewsDao.deleteByCategory(category.apiValue)
        if (articles.isNotEmpty()) {
            cachedNewsDao.insertAll(
                CacheNewsMapper.toCachedNewsEntities(
                    category = category,
                    articles = articles,
                    cachedAt = cachedAt
                )
            )
        }
    }

    suspend fun getCategoryNews(category: NewsCategory): List<NewsArticle> {
        return CacheNewsMapper.toNewsArticles(cachedNewsDao.getByCategory(category.apiValue))
    }

    suspend fun saveNewsDetail(detail: NewsDetail) {
        cachedNewsDetailDao.insert(CacheNewsMapper.toCachedNewsDetailEntity(detail))
    }

    suspend fun getNewsDetail(newsId: String): NewsDetail? {
        return cachedNewsDetailDao.getById(newsId)?.let(CacheNewsMapper::toNewsDetail)
    }

    companion object {
        fun createDefault(context: Context): LocalCacheRepository {
            val database = HomeworkDatabase.getInstance(context)
            return LocalCacheRepository(
                cachedNewsDao = database.cachedNewsDao(),
                cachedNewsDetailDao = database.cachedNewsDetailDao()
            )
        }
    }
}
