package com.example.homework.data.repository

import android.content.Context
import com.example.homework.data.local.HomeworkDatabase
import com.example.homework.data.local.dao.SearchHistoryDao
import com.example.homework.data.local.entity.SearchHistoryEntity
import com.example.homework.data.mapper.NewsMapper
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.search.SearchDataSource
import com.example.homework.data.search.SearchDataSourceFactory
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsCategory
import com.example.homework.model.SearchHistory

class SearchRepository(
    private val searchDataSource: SearchDataSource,
    private val searchHistoryDao: SearchHistoryDao
) {

    suspend fun searchNews(keyword: String): ResultWrapper<List<NewsArticle>> {
        val trimmedKeyword = keyword.trim()
        if (trimmedKeyword.isBlank()) {
            return ResultWrapper.Success(emptyList())
        }

        return when (val result = searchDataSource.searchNews(trimmedKeyword)) {
            is ResultWrapper.Success -> {
                saveSearchHistory(trimmedKeyword)
                ResultWrapper.Success(
                    result.data.map { dto ->
                        NewsMapper.toNewsArticle(
                            dto = dto,
                            fallbackCategory = dto.category?.let(NewsCategory::fromApiValue)
                                ?: NewsCategory.RECOMMEND
                        )
                    }
                )
            }

            is ResultWrapper.Error -> result
        }
    }

    suspend fun getRecentSearchHistory(): List<SearchHistory> {
        return searchHistoryDao.getRecentHistory().map { entity ->
            SearchHistory(
                keyword = entity.keyword,
                lastSearchTime = entity.lastSearchTime
            )
        }
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }

    private suspend fun saveSearchHistory(keyword: String) {
        searchHistoryDao.insert(
            SearchHistoryEntity(
                keyword = keyword,
                lastSearchTime = System.currentTimeMillis()
            )
        )
        searchHistoryDao.trimToLatest10()
    }

    companion object {
        fun createDefault(context: Context): SearchRepository {
            return SearchRepository(
                searchDataSource = SearchDataSourceFactory.createDefault(),
                searchHistoryDao = HomeworkDatabase.getInstance(context).searchHistoryDao()
            )
        }
    }
}
