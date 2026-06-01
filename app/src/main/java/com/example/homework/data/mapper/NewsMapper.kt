package com.example.homework.data.mapper

import com.example.homework.data.remote.dto.NewsArticleDto
import com.example.homework.data.remote.dto.NewsDetailDto
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsDetail
import com.example.homework.model.NewsCategory

object NewsMapper {

    fun toNewsArticle(dto: NewsArticleDto, fallbackCategory: NewsCategory): NewsArticle {
        return NewsArticle(
            id = dto.id ?: "${fallbackCategory.apiValue}-${dto.title.hashCode()}",
            title = dto.title ?: "未命名新闻",
            summary = dto.summary ?: "暂无摘要",
            coverImageUrl = dto.coverImageUrl,
            author = dto.author,
            source = dto.source ?: "校园新闻",
            category = dto.category?.let(NewsCategory::fromApiValue) ?: fallbackCategory,
            publishTime = dto.publishTime ?: "刚刚",
            contentUrl = dto.contentUrl,
            isTop = dto.isTop ?: false
        )
    }

    fun toNewsDetail(dto: NewsDetailDto): NewsDetail {
        val category = dto.category?.let(NewsCategory::fromApiValue) ?: NewsCategory.RECOMMEND
        return NewsDetail(
            id = dto.id ?: "unknown-detail",
            title = dto.title ?: "未命名新闻",
            summary = dto.summary ?: "暂无摘要",
            coverImageUrl = dto.coverImageUrl,
            source = dto.source ?: "校园新闻",
            author = dto.author,
            category = category,
            publishTime = dto.publishTime ?: "刚刚",
            content = dto.content.orEmpty().ifEmpty { listOf("暂无正文内容") },
            contentUrl = dto.contentUrl,
            relatedArticles = dto.relatedArticles.orEmpty().map { articleDto ->
                toNewsArticle(articleDto, category)
            }
        )
    }
}
