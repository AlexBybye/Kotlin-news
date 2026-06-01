package com.example.homework.data.search

import com.example.homework.data.remote.datasource.MockNewsDataSource
import com.example.homework.data.remote.dto.NewsArticleDto
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.model.NewsCategory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MockSearchDataSource(
    private val newsDataSource: MockNewsDataSource = MockNewsDataSource()
) : SearchDataSource {

    override suspend fun searchNews(keyword: String): ResultWrapper<List<NewsArticleDto>> = coroutineScope {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isBlank()) {
            return@coroutineScope ResultWrapper.Success(emptyList())
        }

        val resultList = NewsCategory.entries.map { category ->
            async { newsDataSource.getNews(category) }
        }.map { it.await() }

        val allArticles = mutableListOf<NewsArticleDto>()
        resultList.forEach { result ->
            when (result) {
                is ResultWrapper.Success -> allArticles += result.data.data.orEmpty()
                is ResultWrapper.Error -> return@coroutineScope result
            }
        }

        val matchedArticles = allArticles
            .filter { article ->
                article.matchesKeyword(normalizedKeyword)
            }
            .distinctBy { it.id ?: it.title.orEmpty() }

        ResultWrapper.Success(matchedArticles)
    }

    private fun NewsArticleDto.matchesKeyword(keyword: String): Boolean {
        return title.containsKeyword(keyword) ||
            summary.containsKeyword(keyword) ||
            source.containsKeyword(keyword) ||
            category.containsKeyword(keyword)
    }

    private fun String?.containsKeyword(keyword: String): Boolean {
        return this?.contains(keyword, ignoreCase = true) == true
    }
}
