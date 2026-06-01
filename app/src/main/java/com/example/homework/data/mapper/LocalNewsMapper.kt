package com.example.homework.data.mapper

import com.example.homework.data.local.entity.BrowseHistoryEntity
import com.example.homework.data.local.entity.FavoriteNewsEntity
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsDetail

object LocalNewsMapper {

    fun toFavoriteEntity(detail: NewsDetail): FavoriteNewsEntity {
        return FavoriteNewsEntity(
            newsId = detail.id,
            title = detail.title,
            summary = detail.summary,
            coverImageUrl = detail.coverImageUrl,
            author = detail.author,
            source = detail.source,
            category = detail.category.apiValue,
            publishTime = detail.publishTime,
            contentUrl = detail.contentUrl,
            favoritedAt = System.currentTimeMillis()
        )
    }

    fun toBrowseHistoryEntity(detail: NewsDetail): BrowseHistoryEntity {
        return BrowseHistoryEntity(
            newsId = detail.id,
            title = detail.title,
            summary = detail.summary,
            coverImageUrl = detail.coverImageUrl,
            author = detail.author,
            source = detail.source,
            category = detail.category.apiValue,
            publishTime = detail.publishTime,
            contentUrl = detail.contentUrl,
            lastBrowseTime = System.currentTimeMillis()
        )
    }

    fun toNewsArticle(entity: FavoriteNewsEntity): NewsArticle {
        return NewsArticle(
            id = entity.newsId,
            title = entity.title,
            summary = entity.summary,
            coverImageUrl = entity.coverImageUrl,
            author = entity.author,
            source = entity.source,
            category = entity.category.let(com.example.homework.model.NewsCategory::fromApiValue),
            publishTime = entity.publishTime,
            contentUrl = entity.contentUrl,
            isTop = false
        )
    }

    fun toNewsArticle(entity: BrowseHistoryEntity): NewsArticle {
        return NewsArticle(
            id = entity.newsId,
            title = entity.title,
            summary = entity.summary,
            coverImageUrl = entity.coverImageUrl,
            author = entity.author,
            source = entity.source,
            category = entity.category.let(com.example.homework.model.NewsCategory::fromApiValue),
            publishTime = entity.publishTime,
            contentUrl = entity.contentUrl,
            isTop = false
        )
    }
}
