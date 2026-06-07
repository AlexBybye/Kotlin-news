package com.example.homework.data.repository

import android.content.Context
import com.example.homework.data.mapper.NewsMapper
import com.example.homework.data.remote.datasource.NewsDataSource
import com.example.homework.data.remote.datasource.NewsDataSourceFactory
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsCategory
import com.example.homework.model.NewsDetail

class NewsRepository(
    private val dataSource: NewsDataSource,
    private val localCacheRepository: LocalCacheRepository
) {

    suspend fun getNews(category: NewsCategory): ResultWrapper<CacheAwareData<List<NewsArticle>>> {
        return when (val result = dataSource.getNews(category)) {
            is ResultWrapper.Success -> {
                if (result.data.code != 0) {
                    getCachedNews(
                        category = category,
                        fallbackMessage = result.data.message ?: "新闻数据返回失败"
                    )
                } else {
                    val articles = result.data.data.orEmpty().map { dto ->
                        NewsMapper.toNewsArticle(dto, category)
                    }
                    runCatching {
                        localCacheRepository.saveCategoryNews(category, articles)
                    }
                    ResultWrapper.Success(
                        CacheAwareData(
                            value = articles,
                            isFromCache = false
                        )
                    )
                }
            }

            is ResultWrapper.Error -> getCachedNews(category, result.message)
        }
    }

    /**
     * 聚合各分类的热点新闻，用于「发现」页展示。
     * 优先选取置顶新闻，其次补充各分类首条，去重后返回。
     */
    suspend fun getTrendingNews(limit: Int = 8): ResultWrapper<List<NewsArticle>> {
        val collected = mutableListOf<NewsArticle>()
        var lastError: String? = null

        for (category in NewsCategory.entries) {
            when (val result = getNews(category)) {
                is ResultWrapper.Success -> collected += result.data.value
                is ResultWrapper.Error -> lastError = result.message
            }
        }

        if (collected.isEmpty()) {
            return ResultWrapper.Error(lastError ?: "暂时无法获取热点内容。")
        }

        val trending = collected
            .distinctBy { it.id }
            .sortedByDescending { it.isTop }
            .take(limit)
        return ResultWrapper.Success(trending)
    }

    suspend fun getNewsDetail(newsId: String): ResultWrapper<CacheAwareData<NewsDetail>> {
        return when (val result = dataSource.getNewsDetail(newsId)) {
            is ResultWrapper.Success -> {
                val detail = NewsMapper.toNewsDetail(result.data)
                runCatching {
                    localCacheRepository.saveNewsDetail(detail)
                }
                ResultWrapper.Success(
                    CacheAwareData(
                        value = detail,
                        isFromCache = false
                    )
                )
            }

            is ResultWrapper.Error -> getCachedNewsDetail(newsId, result.message)
        }
    }

    private suspend fun getCachedNews(
        category: NewsCategory,
        fallbackMessage: String
    ): ResultWrapper<CacheAwareData<List<NewsArticle>>> {
        val cachedArticles = localCacheRepository.getCategoryNews(category)
        return if (cachedArticles.isNotEmpty()) {
            ResultWrapper.Success(
                CacheAwareData(
                    value = cachedArticles,
                    isFromCache = true
                )
            )
        } else {
            ResultWrapper.Error(fallbackMessage)
        }
    }

    private suspend fun getCachedNewsDetail(
        newsId: String,
        fallbackMessage: String
    ): ResultWrapper<CacheAwareData<NewsDetail>> {
        val cachedDetail = localCacheRepository.getNewsDetail(newsId)
        return if (cachedDetail != null) {
            ResultWrapper.Success(
                CacheAwareData(
                    value = cachedDetail,
                    isFromCache = true
                )
            )
        } else {
            ResultWrapper.Error(fallbackMessage)
        }
    }

    companion object {
        fun createDefault(context: Context): NewsRepository {
            return NewsRepository(
                dataSource = NewsDataSourceFactory.createDefault(),
                localCacheRepository = LocalCacheRepository.createDefault(context)
            )
        }
    }
}
