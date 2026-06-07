package com.example.homework.data.mapper

import com.example.homework.data.remote.dto.JuheNewsItemDto
import com.example.homework.data.remote.dto.NewsArticleDto
import com.example.homework.data.remote.dto.NewsDetailDto
import com.example.homework.model.NewsCategory

/**
 * 将聚合数据返回的新闻条目转换为应用内部统一的 DTO 结构，
 * 使远程数据源与 Mock 数据源对上层完全一致。
 */
object JuheNewsMapper {

    fun toArticleDto(item: JuheNewsItemDto, category: NewsCategory): NewsArticleDto {
        return NewsArticleDto(
            id = item.uniqueKey ?: item.url ?: item.title.orEmpty(),
            title = item.title,
            summary = buildSummary(item),
            coverImageUrl = item.thumbnailPic,
            author = item.authorName,
            source = item.authorName ?: "头条新闻",
            category = category.apiValue,
            publishTime = item.date,
            contentUrl = item.url,
            isTop = false
        )
    }

    /**
     * 头条接口仅返回列表信息，没有独立详情正文。
     * 这里基于列表条目构造一个详情 DTO，正文提示用户跳转原文阅读。
     */
    fun toDetailDto(item: JuheNewsItemDto, category: NewsCategory): NewsDetailDto {
        return NewsDetailDto(
            id = item.uniqueKey ?: item.url ?: item.title.orEmpty(),
            title = item.title,
            summary = buildSummary(item),
            coverImageUrl = item.thumbnailPic,
            source = item.authorName ?: "头条新闻",
            author = item.authorName,
            category = category.apiValue,
            publishTime = item.date,
            content = listOf(
                "${item.title.orEmpty()}",
                "本条新闻来自「${item.authorName ?: "头条新闻"}」，发布于 ${item.date.orEmpty()}。",
                "点击下方「原文」按钮可在内置浏览器中查看完整报道。"
            ),
            contentUrl = item.url,
            relatedArticles = null
        )
    }

    private fun buildSummary(item: JuheNewsItemDto): String {
        val author = item.authorName ?: "头条新闻"
        val date = item.date.orEmpty()
        return "$author · $date"
    }
}
