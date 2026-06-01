package com.example.homework.data.mapper

import com.example.homework.data.local.entity.CachedNewsDetailEntity
import com.example.homework.data.local.entity.CachedNewsEntity
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsCategory
import com.example.homework.model.NewsDetail
import org.json.JSONArray
import org.json.JSONObject

object CacheNewsMapper {

    fun toCachedNewsEntities(
        category: NewsCategory,
        articles: List<NewsArticle>,
        cachedAt: Long = System.currentTimeMillis()
    ): List<CachedNewsEntity> {
        return articles.mapIndexed { index, article ->
            CachedNewsEntity(
                newsId = article.id,
                category = category.apiValue,
                title = article.title,
                summary = article.summary,
                coverImageUrl = article.coverImageUrl,
                author = article.author,
                source = article.source,
                publishTime = article.publishTime,
                contentUrl = article.contentUrl,
                isTop = article.isTop,
                displayOrder = index,
                cachedAt = cachedAt
            )
        }
    }

    fun toNewsArticles(entities: List<CachedNewsEntity>): List<NewsArticle> {
        return entities.map { entity ->
            NewsArticle(
                id = entity.newsId,
                title = entity.title,
                summary = entity.summary,
                coverImageUrl = entity.coverImageUrl,
                author = entity.author,
                source = entity.source,
                category = NewsCategory.fromApiValue(entity.category),
                publishTime = entity.publishTime,
                contentUrl = entity.contentUrl,
                isTop = entity.isTop
            )
        }
    }

    fun toCachedNewsDetailEntity(
        detail: NewsDetail,
        cachedAt: Long = System.currentTimeMillis()
    ): CachedNewsDetailEntity {
        return CachedNewsDetailEntity(
            newsId = detail.id,
            title = detail.title,
            summary = detail.summary,
            coverImageUrl = detail.coverImageUrl,
            author = detail.author,
            source = detail.source,
            category = detail.category.apiValue,
            publishTime = detail.publishTime,
            contentUrl = detail.contentUrl,
            contentJson = JSONArray(detail.content).toString(),
            relatedArticlesJson = serializeRelatedArticles(detail.relatedArticles),
            cachedAt = cachedAt
        )
    }

    fun toNewsDetail(entity: CachedNewsDetailEntity): NewsDetail {
        val category = NewsCategory.fromApiValue(entity.category)
        return NewsDetail(
            id = entity.newsId,
            title = entity.title,
            summary = entity.summary,
            coverImageUrl = entity.coverImageUrl,
            source = entity.source,
            author = entity.author,
            category = category,
            publishTime = entity.publishTime,
            content = deserializeContent(entity.contentJson),
            contentUrl = entity.contentUrl,
            relatedArticles = deserializeRelatedArticles(entity.relatedArticlesJson, category)
        )
    }

    private fun serializeRelatedArticles(articles: List<NewsArticle>): String {
        val array = JSONArray()
        articles.forEach { article ->
            array.put(
                JSONObject()
                    .put("id", article.id)
                    .put("title", article.title)
                    .put("summary", article.summary)
                    .put("coverImageUrl", article.coverImageUrl)
                    .put("author", article.author)
                    .put("source", article.source)
                    .put("category", article.category.apiValue)
                    .put("publishTime", article.publishTime)
                    .put("contentUrl", article.contentUrl)
                    .put("isTop", article.isTop)
            )
        }
        return array.toString()
    }

    private fun deserializeContent(contentJson: String): List<String> {
        return runCatching {
            val array = JSONArray(contentJson)
            List(array.length()) { index -> array.optString(index) }
                .filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
            .ifEmpty { listOf("暂无正文内容") }
    }

    private fun deserializeRelatedArticles(
        relatedArticlesJson: String,
        fallbackCategory: NewsCategory
    ): List<NewsArticle> {
        return runCatching {
            val array = JSONArray(relatedArticlesJson)
            List(array.length()) { index ->
                array.optJSONObject(index)?.let { articleJson ->
                    NewsArticle(
                        id = articleJson.optString("id"),
                        title = articleJson.optString("title", "未命名新闻"),
                        summary = articleJson.optString("summary", "暂无摘要"),
                        coverImageUrl = articleJson.optString("coverImageUrl").takeIf { it.isNotBlank() },
                        author = articleJson.optString("author").takeIf { it.isNotBlank() },
                        source = articleJson.optString("source", "校园新闻"),
                        category = NewsCategory.fromApiValue(
                            articleJson.optString("category").ifBlank { fallbackCategory.apiValue }
                        ),
                        publishTime = articleJson.optString("publishTime", "刚刚"),
                        contentUrl = articleJson.optString("contentUrl").takeIf { it.isNotBlank() },
                        isTop = articleJson.optBoolean("isTop", false)
                    )
                }
            }.filterNotNull()
        }.getOrDefault(emptyList())
    }
}
